package dev.utils.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import dev.utils.JCLogUtils;

/**
 * detail: 流操作工具类
 * @author Ttt
 */
public final class StreamUtils {

    private StreamUtils() {
    }

    // 日志 TAG
    private static final String TAG = StreamUtils.class.getSimpleName();

    /**
     * 输入流转输出流
     * @param inputStream {@link InputStream}
     * @return {@link ByteArrayOutputStream}
     */
    public static ByteArrayOutputStream inputToOutputStream(final InputStream inputStream) {
        if (inputStream == null) return null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer, 0, 1024)) != -1) {
                baos.write(buffer, 0, len);
            }
            return baos;
        } catch (Exception e) {
            JCLogUtils.eTag(TAG, e, "inputToOutputStream");
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * 输出流转输入流
     * @param outputStream {@link OutputStream}
     * @return {@link ByteArrayInputStream}
     */
    public static ByteArrayInputStream outputToInputStream(final OutputStream outputStream) {
        if (outputStream == null) return null;
        try {
            return new ByteArrayInputStream(((ByteArrayOutputStream) outputStream).toByteArray());
        } catch (Exception e) {
            JCLogUtils.eTag(TAG, e, "outputToInputStream");
            return null;
        }
    }

    /**
     * 输入流转 byte[]
     * @param inputStream {@link InputStream}
     * @return byte[]
     */
    public static byte[] inputStreamToBytes(final InputStream inputStream) {
        if (inputStream == null) return null;
        try {
            return inputToOutputStream(inputStream).toByteArray();
        } catch (Exception e) {
            JCLogUtils.eTag(TAG, e, "inputStreamToBytes");
            return null;
        }
    }

    /**
     * byte[] 转输出流
     * @param bytes 数据源
     * @return {@link InputStream}
     */
    public static InputStream bytesToInputStream(final byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        try {
            return new ByteArrayInputStream(bytes);
        } catch (Exception e) {
            JCLogUtils.eTag(TAG, e, "bytesToInputStream");
            return null;
        }
    }

    /**
     * 输出流转 byte[]
     * @param outputStream {@link OutputStream}
     * @return byte[]
     */
    public static byte[] outputStreamToBytes(final OutputStream outputStream) {
        if (outputStream == null) return null;
        try {
            return ((ByteArrayOutputStream) outputStream).toByteArray();
        } catch (Exception e) {
            JCLogUtils.eTag(TAG, e, "outputStreamToBytes");
            return null;
        }
    }

    /**
     * byte[] 转 输出流
     * @param bytes 数据源
     * @return {@link OutputStream}
     */
    public static OutputStream bytesToOutputStream(final byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            baos.write(bytes);
            return baos;
        } catch (Exception e) {
            JCLogUtils.eTag(TAG, e, "bytesToOutputStream");
            return null;
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * 输入流转 String
     * @param inputStream {@link InputStream}
     * @param charsetName 编码格式
     * @return 指定编码字符串
     */
    public static String inputStreamToString(final InputStream inputStream, final String charsetName) {
        if (inputStream == null || isSpace(charsetName)) return null;
        try {
            return new String(inputStreamToBytes(inputStream), charsetName);
        } catch (Exception e) {
            JCLogUtils.eTag(TAG, e, "inputStreamToString");
            return null;
        }
    }

    /**
     * String 转换输入流
     * @param string      数据源
     * @param charsetName 编码格式
     * @return {@link InputStream}
     */
    public static InputStream stringToInputStream(final String string, final String charsetName) {
        if (string == null || isSpace(charsetName)) return null;
        try {
            return new ByteArrayInputStream(string.getBytes(charsetName));
        } catch (Exception e) {
            JCLogUtils.eTag(TAG, e, "stringToInputStream");
            return null;
        }
    }

    /**
     * 输出流转 String
     * @param outputStream {@link OutputStream}
     * @param charsetName  编码格式
     * @return 指定编码字符串
     */
    public static String outputStreamToString(final OutputStream outputStream, final String charsetName) {
        if (outputStream == null || isSpace(charsetName)) return null;
        try {
            return new String(outputStreamToBytes(outputStream), charsetName);
        } catch (Exception e) {
            JCLogUtils.eTag(TAG, e, "outputStreamToString");
            return null;
        }
    }

    /**
     * String 转 输出流
     * @param string      数据源
     * @param charsetName 编码格式
     * @return {@link OutputStream}
     */
    public static OutputStream stringToOutputStream(final String string, final String charsetName) {
        if (string == null || isSpace(charsetName)) return null;
        try {
            return bytesToOutputStream(string.getBytes(charsetName));
        } catch (Exception e) {
            JCLogUtils.eTag(TAG, e, "stringToOutputStream");
            return null;
        }
    }

    // ======================
    // = 其他工具类实现代码 =
    // ======================

    // ===============
    // = StringUtils =
    // ===============

    /**
     * 判断字符串是否为 null 或全为空白字符
     * @param str 待校验字符串
     * @return {@code true} yes, {@code false} no
     */
    private static boolean isSpace(final String str) {
        if (str == null) return true;
        for (int i = 0, len = str.length(); i < len; ++i) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
