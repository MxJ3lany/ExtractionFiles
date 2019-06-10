/*
 * Copyright (C) 2017 Peng fei Pan <sky@panpf.me>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.panpf.tool4j.util;

import android.util.Base64;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;

/**
 * DES加解密工具
 */
public class DESCoder {
    private Key key;

    /**
     * 创建一个DEC加解密工具对象
     *
     * @param key 密匙
     */
    public DESCoder(String key) {
        setKey(key);
    }

    /**
     * 加密字节数组
     *
     * @param encryptByteArray 待加密的字节数组
     * @return 加密后的字节数组
     */
    private byte[] encryptByteArray(byte[] encryptByteArray) {
        try {
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(encryptByteArray);
        } catch (Exception e) {
            throw new RuntimeException("Error initializing SqlMap class. Cause: " + e);
        }
    }

    /**
     * 解密字节数组
     *
     * @param decryptByteArray 待解密的字节数组
     * @return 解密后的字节数组
     */
    private byte[] decryptByte(byte[] decryptByteArray) {
        try {
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(decryptByteArray);
        } catch (Exception e) {
            throw new RuntimeException("Error initializing SqlMap class. Cause: " + e);
        }
    }

    /**
     * 加密字符串
     *
     * @param encryptContent 待加密的字符串
     * @return 加密后的字符串
     */
    public String encryptString(String encryptContent) {
        try {
            return Base64.encodeToString(encryptByteArray(encryptContent.getBytes("UTF8")), Base64.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException("Error initializing SqlMap class. Cause: " + e);
        }
    }

    /**
     * 解密字符串
     *
     * @param decryptContent 待解密的字节数组
     * @return 解密后的字节数组
     */
    public String decryptString(String decryptContent) {
        try {
            return new String(decryptByte(Base64.decode(decryptContent, Base64.DEFAULT)), "UTF8");
        } catch (Exception e) {
            throw new RuntimeException("Error initializing SqlMap class. Cause: " + e);
        }
    }

    /**
     * 文件 file 进行加密并保存目标文件 destFile 中
     *
     * @param file     要加密的文件 如 c:/test/srcFile.txt
     * @param destFile 加密后存放的文件名 如 c:/ 加密后文件 .txt
     */
    public boolean encryptFile(String file, String destFile) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            inputStream = new CipherInputStream(new FileInputStream(file), cipher);

            outputStream = new FileOutputStream(destFile);

            byte[] buffer = new byte[1024];
            int numberr;
            while ((numberr = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, numberr);
            }

            inputStream.close();
            outputStream.flush();
            outputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            return false;
        }
    }

    /**
     * 文件采用 DES 算法解密文件
     *
     * @param file 已加密的文件 如 c:/ 加密后文件 .txt *
     * @param dest 解密后存放的文件名 如 c:/ test/ 解密后文件 .txt
     */
    public boolean decryptFile(String file, String dest) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.DECRYPT_MODE, this.key);
            inputStream = new FileInputStream(file);

            outputStream = new CipherOutputStream(new FileOutputStream(dest), cipher);

            byte[] buffer = new byte[1024];
            int r;
            while ((r = inputStream.read(buffer)) >= 0) {
                outputStream.write(buffer, 0, r);
            }
            inputStream.close();
            outputStream.flush();
            outputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            return false;
        }
    }

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    /**
     * 根据参数生成 KEY
     *
     * @param stringKey
     */
    public void setKey(String stringKey) {
        try {
            KeyGenerator generator = KeyGenerator.getInstance("DES");
            generator.init(new SecureRandom(stringKey.getBytes()));
            this.key = generator.generateKey();
            generator = null;
        } catch (Exception e) {
            throw new RuntimeException("Error initializing SqlMap class. Cause: " + e);
        }
    }
}