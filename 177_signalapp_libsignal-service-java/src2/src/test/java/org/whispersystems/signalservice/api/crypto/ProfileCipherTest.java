package org.whispersystems.signalservice.api.crypto;


import junit.framework.TestCase;

import org.conscrypt.Conscrypt;
import org.whispersystems.signalservice.internal.util.Util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.Security;

public class ProfileCipherTest extends TestCase {

  static {
    Security.insertProviderAt(Conscrypt.newProvider(), 1);
  }

  public void testEncryptDecrypt() throws InvalidCiphertextException {
    byte[]        key       = Util.getSecretBytes(32);
    ProfileCipher cipher    = new ProfileCipher(key);
    byte[]        name      = cipher.encryptName("Clement Duval".getBytes(), 26);
    byte[]        plaintext = cipher.decryptName(name);
    assertEquals(new String(plaintext), "Clement Duval");
  }

  public void testEmpty() throws Exception {
    byte[]        key       = Util.getSecretBytes(32);
    ProfileCipher cipher    = new ProfileCipher(key);
    byte[]        name      = cipher.encryptName("".getBytes(), 26);
    byte[]        plaintext = cipher.decryptName(name);

    assertEquals(plaintext.length, 0);
  }

  public void testStreams() throws Exception {
    byte[]                    key  = Util.getSecretBytes(32);
    ByteArrayOutputStream     baos = new ByteArrayOutputStream();
    ProfileCipherOutputStream out  = new ProfileCipherOutputStream(baos, key);

    out.write("This is an avatar".getBytes());
    out.flush();
    out.close();

    ByteArrayInputStream     bais = new ByteArrayInputStream(baos.toByteArray());
    ProfileCipherInputStream in   = new ProfileCipherInputStream(bais, key);

    ByteArrayOutputStream result = new ByteArrayOutputStream();
    byte[]                buffer = new byte[2048];

    int read;

    while ((read = in.read(buffer)) != -1) {
      result.write(buffer, 0, read);
    }

    assertEquals(new String(result.toByteArray()), "This is an avatar");
  }

}
