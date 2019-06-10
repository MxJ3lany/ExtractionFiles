// Copyright (C) 2011 - Will Glozer.  All rights reserved.

package com.mrd.bitlib.lambdaworks.crypto;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.System.arraycopy;

import java.security.GeneralSecurityException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * An implementation of the <a
 * href="http://www.tarsnap.com/scrypt/scrypt.pdf"/>scrypt</a> key derivation
 * function.
 * 
 * @author Will Glozer
 * Note: Removed native JNI calls for native implementation
 *         for use with BCCAPI
 */
public class SCrypt {
   /**
    * Pure Java implementation of the <a
    * href="http://www.tarsnap.com/scrypt/scrypt.pdf"/>scrypt KDF</a>.
    * 
    * @param passwd
    *           Password.
    * @param salt
    *           Salt.
    * @param N
    *           CPU cost parameter.
    * @param r
    *           Memory cost parameter.
    * @param p
    *           Parallelization parameter.
    * @param dkLen
    *           Intended length of the derived key.
    * 
    * @return The derived key.
    * 
    * @throws GeneralSecurityException
    *            when HMAC_SHA256 is not available.
    * @throws InterruptedException
    */
   public static byte[] scrypt(byte[] passwd, byte[] salt, int N, int r, int p, int dkLen,
                                 SCryptProgress progressTracker) throws GeneralSecurityException, InterruptedException {
      if (N == 0 || (N & (N - 1)) != 0)
         throw new IllegalArgumentException("N must be > 0 and a power of 2");

      if (N > MAX_VALUE / 128 / r)
         throw new IllegalArgumentException("Parameter N is too large");
      if (r > MAX_VALUE / 128 / p)
         throw new IllegalArgumentException("Parameter r is too large");

      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(passwd, "HmacSHA256"));

      byte[] DK = new byte[dkLen];

      byte[] B = new byte[128 * r * p];
      byte[] XY = new byte[256 * r];
      // The original SCrypt implementation uses one big V array with N * 128 *
      // r bytes. We have observed that this may cause problems on some android
      // devices due to memory fragmentation. Instead we allocate N arrays of
      // size 128 * r.
      byte[][] V = new byte[N][];
      for (int i = 0; i < N; i++) {
         V[i] = new byte[128 * r];
      }
      
      int i;

      PBKDF.pbkdf2(mac, salt, 1, B, p * 128 * r);

      for (i = 0; i < p; i++) {
         smix(B, i * 128 * r, r, N, V, XY, progressTracker);
         if (progressTracker != null) {
            progressTracker.setProgressP(i+1);
         }
      }
      PBKDF.pbkdf2(mac, B, 1, DK, dkLen);

      return DK;
   }

   private static void smix(byte[] B, int Bi, int r, int N, byte[][] V, byte[] XY, SCryptProgress progressTracker)
         throws InterruptedException {
      int Xi = 0;
      int Yi = 128 * r;
      int i;

      arraycopy(B, Bi, XY, Xi, 128 * r);

      for (i = 0; i < N; i++) {
         arraycopy(XY, Xi, V[i], 0, 128 * r);
         blockmix_salsa8(XY, Xi, Yi, r);
         if (progressTracker != null) {
            progressTracker.setProgressN1(i);
         }
      }

      for (i = 0; i < N; i++) {
         int j = integerify(XY, Xi, r) & (N - 1);
         blockxor(V[j], 0, XY, Xi, 128 * r);
         blockmix_salsa8(XY, Xi, Yi, r);
         if (progressTracker != null) {
            progressTracker.setProgressN2(i);
         }
      }

      arraycopy(XY, Xi, B, Bi, 128 * r);
   }

   private static void blockmix_salsa8(byte[] BY, int Bi, int Yi, int r) {
      byte[] X = new byte[64];
      int i;

      arraycopy(BY, Bi + (2 * r - 1) * 64, X, 0, 64);

      for (i = 0; i < 2 * r; i++) {
         blockxor(BY, i * 64, X, 0, 64);
         salsa20_8(X);
         arraycopy(X, 0, BY, Yi + (i * 64), 64);
      }

      for (i = 0; i < r; i++) {
         arraycopy(BY, Yi + (i * 2) * 64, BY, Bi + (i * 64), 64);
      }

      for (i = 0; i < r; i++) {
         arraycopy(BY, Yi + (i * 2 + 1) * 64, BY, Bi + (i + r) * 64, 64);
      }
   }

   public static int R(int a, int b) {
      return (a << b) | (a >>> (32 - b));
   }

   private static void salsa20_8(byte[] B) {
      int[] B32 = new int[16];
      int[] x = new int[16];
      int i;

      for (i = 0; i < 16; i++) {
         B32[i] = (B[i * 4 + 0] & 0xff) << 0;
         B32[i] |= (B[i * 4 + 1] & 0xff) << 8;
         B32[i] |= (B[i * 4 + 2] & 0xff) << 16;
         B32[i] |= (B[i * 4 + 3] & 0xff) << 24;
      }

      arraycopy(B32, 0, x, 0, 16);

      for (i = 8; i > 0; i -= 2) {
         x[4] ^= R(x[0] + x[12], 7);
         x[8] ^= R(x[4] + x[0], 9);
         x[12] ^= R(x[8] + x[4], 13);
         x[0] ^= R(x[12] + x[8], 18);
         x[9] ^= R(x[5] + x[1], 7);
         x[13] ^= R(x[9] + x[5], 9);
         x[1] ^= R(x[13] + x[9], 13);
         x[5] ^= R(x[1] + x[13], 18);
         x[14] ^= R(x[10] + x[6], 7);
         x[2] ^= R(x[14] + x[10], 9);
         x[6] ^= R(x[2] + x[14], 13);
         x[10] ^= R(x[6] + x[2], 18);
         x[3] ^= R(x[15] + x[11], 7);
         x[7] ^= R(x[3] + x[15], 9);
         x[11] ^= R(x[7] + x[3], 13);
         x[15] ^= R(x[11] + x[7], 18);
         x[1] ^= R(x[0] + x[3], 7);
         x[2] ^= R(x[1] + x[0], 9);
         x[3] ^= R(x[2] + x[1], 13);
         x[0] ^= R(x[3] + x[2], 18);
         x[6] ^= R(x[5] + x[4], 7);
         x[7] ^= R(x[6] + x[5], 9);
         x[4] ^= R(x[7] + x[6], 13);
         x[5] ^= R(x[4] + x[7], 18);
         x[11] ^= R(x[10] + x[9], 7);
         x[8] ^= R(x[11] + x[10], 9);
         x[9] ^= R(x[8] + x[11], 13);
         x[10] ^= R(x[9] + x[8], 18);
         x[12] ^= R(x[15] + x[14], 7);
         x[13] ^= R(x[12] + x[15], 9);
         x[14] ^= R(x[13] + x[12], 13);
         x[15] ^= R(x[14] + x[13], 18);
      }

      for (i = 0; i < 16; ++i)
         B32[i] = x[i] + B32[i];

      for (i = 0; i < 16; i++) {
         B[i * 4 + 0] = (byte) (B32[i] >> 0 & 0xff);
         B[i * 4 + 1] = (byte) (B32[i] >> 8 & 0xff);
         B[i * 4 + 2] = (byte) (B32[i] >> 16 & 0xff);
         B[i * 4 + 3] = (byte) (B32[i] >> 24 & 0xff);
      }
   }

   private static void blockxor(byte[] S, int Si, byte[] D, int Di, int len) {
      for (int i = 0; i < len; i++) {
         D[Di + i] ^= S[Si + i];
      }
   }

   private static int integerify(byte[] B, int Bi, int r) {
      int n;

      Bi += (2 * r - 1) * 64;

      n = (B[Bi + 0] & 0xff) << 0;
      n |= (B[Bi + 1] & 0xff) << 8;
      n |= (B[Bi + 2] & 0xff) << 16;
      n |= (B[Bi + 3] & 0xff) << 24;

      return n;
   }
}
