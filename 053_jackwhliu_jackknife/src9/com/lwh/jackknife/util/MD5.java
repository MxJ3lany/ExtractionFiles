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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5 {

    static char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private MD5() {
    }

    public static String getMD5(String inStr) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
            return "";
        }
        char[] charArray = inStr.toCharArray();
        byte[] byteArray = new byte[charArray.length];
        for (int i = 0; i < charArray.length; i++)
            byteArray[i] = (byte) charArray[i];
        byte[] md5Bytes = md5.digest(byteArray);
        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16)
                hexValue.append("0");
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }

    public static String getMessageDigest(byte[] buffer) {
        try {
            MessageDigest mdTemp = MessageDigest.getInstance("MD5");
            mdTemp.update(buffer);
            byte[] md = mdTemp.digest();
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getMD5(File file) {
        FileInputStream fis = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            fis = new FileInputStream(file);
            byte[] buffer = new byte[2048];
            int length;
            while ((length = fis.read(buffer)) != -1) {
                md.update(buffer, 0, length);
            }
            byte[] b = md.digest();
            return byteToHexString(b);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            try {
                fis.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static String byteToHexString(byte[] tmp) {
        String s;
        char str[] = new char[16 * 2];
        int k = 0;
        for (int i = 0; i < 16; i++) {
            byte byte0 = tmp[i];
            str[k++] = hexDigits[byte0 >>> 4 & 0xf];
            str[k++] = hexDigits[byte0 & 0xf];
        }
        s = new String(str);
        return s;
    }

    private static synchronized MessageDigest checkAlgorithm() {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new NullPointerException("No md5 algorithm found");
        }
        return messageDigest;
    }

    public static String digest32(String src) {
        if (src == null) {
            return null;
        }
        MessageDigest messageDigest = checkAlgorithm();
        byte[] ret = null;
        try {
            ret = messageDigest.digest(src.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return ret == null ? null : IoUtils.bs2H(ret);
    }

    public static String digest32(String src, String charset) {
        if (src == null) {
            return null;
        }
        MessageDigest messageDigest = checkAlgorithm();
        byte[] ret = null;
        try {
            ret = messageDigest.digest(src.getBytes(charset));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return ret == null ? null : IoUtils.bs2H(ret);
    }

    public static String digest32(File src) throws IOException {
        if (src == null) {
            return null;
        }
        MessageDigest messageDigest = checkAlgorithm();
        InputStream fis = null;
        DigestInputStream dis = null;
        try {
            fis = new FileInputStream(src);
            dis = new DigestInputStream(fis, messageDigest);
            byte[] buffer = new byte[2048];
            while (dis.read(buffer) > 0) ;
            messageDigest = dis.getMessageDigest();
        } finally {
            if (fis != null) {
                fis.close();
            }
            if (dis != null) {
                dis.close();
            }
        }
        return IoUtils.bs2H(messageDigest.digest());
    }

    public static String digest32(byte[] src) {
        if (src == null) {
            return null;
        }
        MessageDigest messageDigest = checkAlgorithm();
        byte[] ret = messageDigest.digest(src);
        return ret == null ? null : IoUtils.bs2H(ret);
    }

    public static String digest16(String src) {
        String encrypt = digest32(src);
        return encrypt == null ? null : encrypt.substring(8, 24);
    }

    public static String digest16(String src, String charset) {
        String encrypt = digest32(src, charset);
        return encrypt == null ? null : encrypt.substring(8, 24);
    }

    public static String digest16(File src) throws IOException {
        String encrypt = digest32(src);
        return encrypt == null ? null : encrypt.substring(8, 24);
    }

    public static String digest16(byte[] src) {
        String encrypt = digest32(src);
        return encrypt == null ? null : encrypt.substring(8, 24);
    }
}
