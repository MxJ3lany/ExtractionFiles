/*
 * Copyright (C) 2018 The JackKnife Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lwh.jackknife.util;

import android.text.TextUtils;

import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class DES {

    private static final byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0};

    public static String encryptDES(String KEY_DES, String encryptString) throws Exception {
        IvParameterSpec zeroIv = new IvParameterSpec(iv);
        SecretKeySpec key = new SecretKeySpec(KEY_DES.getBytes(), "DES");
        Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, zeroIv);
        byte[] encryptedData = cipher.doFinal(encryptString.getBytes());
        return parseByte2HexStr(encryptedData);
    }

    public static String encryptDES(String keyString, int keyPos, String encryptString, String ivString, int ivPos) throws Exception {
        String keyValue = keyString.substring(keyPos - 1, 17);
        String ivValue = ivString.substring(ivPos - 1, 25);
        IvParameterSpec zeroIv = new IvParameterSpec(ivValue.getBytes());
        SecretKeySpec key = new SecretKeySpec(keyValue.getBytes(), "DES");
        Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, zeroIv);
        byte[] encryptedData = cipher.doFinal(encryptString.getBytes());
        String encryptedStr = parseByte2HexStr(encryptedData);
        return encryptedStr;
    }

    public static String parseByte2HexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            Locale loc = Locale.getDefault();
            sb.append(hex.toUpperCase(loc));
        }
        return sb.toString();
    }

    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        Locale loc = Locale.getDefault();
        hexString = hexString.toUpperCase(loc);
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static String decryptDES(String keyString, String decryptString) throws Exception {
        if (!TextUtils.isEmpty(decryptString)) {

            byte[] byteMi = hexStringToBytes(decryptString);
            IvParameterSpec zeroIv = new IvParameterSpec(iv);
            byte[] byteKey = keyString.getBytes();
            SecretKeySpec key = new SecretKeySpec(byteKey, "DES");
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, zeroIv);
            byte decryptedData[] = cipher.doFinal(byteMi);
            return new String(decryptedData);
        }
        return "";
    }

    public static String decryptDES(String keyString, int keyPos, String decryptString, String ivString, int ivPos) throws Exception {
        if (!TextUtils.isEmpty(decryptString)) {
            String keyValue = keyString.substring(keyPos - 1, 16);
            String ivValue = ivString.substring(ivPos - 1, 24);
            byte[] byteMi = hexStringToBytes(decryptString);
            IvParameterSpec zeroIv = new IvParameterSpec(ivValue.getBytes());
            SecretKeySpec key = new SecretKeySpec(keyValue.getBytes(), "DES");
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, zeroIv);
            byte decryptedData[] = cipher.doFinal(byteMi);
            return new String(decryptedData);
        }
        return "";
    }
}
