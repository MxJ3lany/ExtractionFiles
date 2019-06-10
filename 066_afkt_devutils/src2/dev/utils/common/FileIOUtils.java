package dev.utils.common;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import dev.utils.JCLogUtils;

/**
 * detail: 文件(IO流)工具类
 * @author Ttt
 */
public final class FileIOUtils {

    private FileIOUtils() {
    }

    // 日志 TAG
    private static final String TAG = FileIOUtils.class.getSimpleName();
    // 换行符
    private static final String NEW_LINE_STR = System.getProperty("line.separator");
    // 缓存大小
    private static int sBufferSize = 8192;

    /**
     * 设置缓冲区的大小, 默认大小等于 8192 字节
     * @param bufferSize 缓冲 Buffer 大小
     */
    public static void setBufferSize(final int bufferSize) {
        sBufferSize = bufferSize;
    }

    /**
     * 通过输入流写入文件
     * @param filePath    文件路径
     * @param inputStream {@link InputStream}
     * @return {@code true} success, {@code false} fail
     */
    public static boolean writeFileFromIS(final String filePath, final InputStream inputStream) {
        return writeFileFromIS(getFileByPath(filePath), inputStream, false);
    }

    /**
     * 通过输入流写入文件
     * @param filePath    文件路径
     * @param inputStream {@link InputStream}
     * @param append      是否追加到结尾
     * @return {@code true} success, {@code false} fail
     */
    public static boolean writeFileFromIS(final String filePath, final InputStream inputStream, final boolean append) {
        return writeFileFromIS(getFileByPath(filePath), inputStream, append);
    }

    /**
     * 通过输入流写入文件
     * @param file        文件
     * @param inputStream {@link InputStream}
     * @return {@code true} success, {@code false} fail
     */
    public static boolean writeFileFromIS(final File file, final InputStream inputStream) {
        return writeFileFromIS(file, inputStream, false);
    }

    /**
     * 通过输入流写入文件
     * @param file        文件
     * @param inputStream {@link InputStream}
     * @param append      是否追加到结尾
     * @return {@code true} success, {@code false} fail
     */
    public static boolean writeFileFromIS(final File file, final InputStream inputStream, final boolean append) {
        if (!createOrExistsFile(file) || inputStream == null) return false;
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(file, append));
            byte[] data = new byte[sBufferSize];
            int len;
            while ((len = inputStream.read(data, 0, sBufferSize)) != -1) {
                os.write(data, 0, len);
            }
            return true;
        } catch (IOException e) {
            JCLogUtils.eTag(TAG, e, "writeFileFromIS");
            return false;
        } finally {
            closeIOQuietly(inputStream, os);
        }
    }

    /**
     * 通过字节流写入文件
     * @param filePath 文件路径
     * @param bytes    byte[]
     * @return {@code true} success, {@code false} fail
     */
    public static boolean writeFileFromBytesByStream(final String filePath, final byte[] bytes) {
        return writeFileFromBytesByStream(getFileByPath(filePath), bytes, false);
    }

    /**
     * 通过字节流写入文件
     * @param filePath 文件路径
     * @param bytes    byte[]
     * @param append   是否追加到结尾
     * @return {@code true} success, {@code false} fail
     */
    public static boolean writeFileFromBytesByStream(final String filePath, final byte[] bytes, final boolean append) {
        return writeFileFromBytesByStream(getFileByPath(filePath), bytes, append);
    }

    /**
     * 通过字节流写入文件
     * @param file  文件
     * @param bytes byte[]
     * @return {@code true} success, {@code false} fail
     */
    public static boolean writeFileFromBytesByStream(final File file, final byte[] bytes) {
        return writeFileFromBytesByStream(file, bytes, false);
    }

    /**
     * 通过字节流写入文件
     * @param file   文件
     * @param bytes  byte[]
     * @param append 是否追加到结尾
     * @return {@code true} success, {@code false} fail
     */
    public static boolean writeFileFromBytesByStream(final File file, final byte[] bytes, final boolean append) {
        if (bytes == null || !createOrExistsFile(file)) return false;
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(file, append));
            bos.write(bytes);
            return true;
        } catch (IOException e) {
            JCLogUtils.eTag(TAG, e, "writeFileFromBytesByStream");
            return false;
        } finally {
            closeIOQuietly(bos);
        }
    }

    /**
     * 通过 FileChannel 把字节流写入文件
     * @param filePath 文件路径
     * @param bytes    byte[]
     * @param isForce  是否强制写入
     * @return {@code true} success, {@code false} fail
     */
    public static boolean writeFileFromBytesByChannel(final String filePath, final byte[] bytes, final boolean isForce) {
        return writeFileFromBytesByChannel(getFileByPath(filePath), bytes, false, isForce);
    }

    /**
     * 通过 FileChannel 把字节流写入文件
     * @param filePath 文件路径
     * @param bytes    byte[]
     * @param append   是否追加到结尾
     * @param isForce  是否强制写入
     * @return {@code true} success, {@code false} fail
     */
    public static boolean writeFileFromBytesByChannel(final String filePath, final byte[] bytes, final boolean append, final boolean isForce) {
        return writeFileFromBytesByChannel(getFileByPath(filePath), bytes, append, isForce);
    }

    /**
     * 通过 FileChannel 把字节流写入文件
     * @param file    文件
     * @param bytes   byte[]
     * @param isForce 是否强制写入
     * @return {@code true} success, {@code false} fail
     */
    public static boolean writeFileFromBytesByChannel(final File file, final byte[] bytes, final boolean isForce) {
        return writeFileFromBytesByChannel(file, bytes, false, isForce);
    }

    /**
     * 通过 FileChannel 把字节流写入文件
     * @param file    文件
     * @param bytes   byte[]
     * @param append  是否追加到结尾
     * @param isForce 是否强制写入
     * @return {@code true} success, {@code false} fail
     */
    public static boolean writeFileFromBytesByChannel(final File file, final byte[] bytes, final boolean append, final boolean isForce) {
        if (bytes == null) return false;
        FileChannel fc = null;
        try {
            fc = new FileOutputStream(file, append).getChannel();
            fc.position(fc.size());
            fc.write(ByteBuffer.wrap(bytes));
            if (isForce) fc.force(true);
            return true;
        } catch (IOException e) {
            JCLogUtils.eTag(TAG, e, "writeFileFromBytesByChannel");
            return false;
        } finally {
            closeIOQuietly(fc);
        }
    }

    /**
     * 通过 MappedByteBuffer 把字节流写入文件
     * @param filePath 文件路径
     * @param bytes    byte[]
     * @param isForce  是否强制写入
     * @return {@code true} success, {@code false} fail
     */
    public static boolean writeFileFromBytesByMap(final String filePath, final byte[] bytes, final boolean isForce) {
        return writeFileFromBytesByMap(filePath, bytes, false, isForce);
    }

    /**
     * 通过 MappedByteBuffer 把字节流写入文件
     * @param filePath 文件路径
     * @param bytes    byte[]
     * @param append   是否追加到结尾
     * @param isForce  是否强制写入
     * @return {@code true} success, {@code false} fail
     */
    public static boolean writeFileFromBytesByMap(final String filePath, final byte[] bytes, final boolean append, final boolean isForce) {
        return writeFileFromBytesByMap(getFileByPath(filePath), bytes, append, isForce);
    }

    /**
     * 通过 MappedByteBuffer 把字节流写入文件
     * @param file    文件
     * @param bytes   byte[]
     * @param isForce 是否强制写入
     * @return {@code true} success, {@code false} fail
     */
    public static boolean writeFileFromBytesByMap(final File file, final byte[] bytes, final boolean isForce) {
        return writeFileFromBytesByMap(file, bytes, false, isForce);
    }

    /**
     * 通过 MappedByteBuffer 把字节流写入文件
     * @param file    文件
     * @param bytes   byte[]
     * @param append  是否追加到结尾
     * @param isForce 是否强制写入
     * @return {@code true} success, {@code false} fail
     */
    public static boolean writeFileFromBytesByMap(final File file, final byte[] bytes, final boolean append, final boolean isForce) {
        if (bytes == null || !createOrExistsFile(file)) return false;
        FileChannel fc = null;
        try {
            fc = new FileOutputStream(file, append).getChannel();
            MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE, fc.size(), bytes.length);
            mbb.put(bytes);
            if (isForce) mbb.force();
            return true;
        } catch (IOException e) {
            JCLogUtils.eTag(TAG, e, "writeFileFromBytesByMap");
            return false;
        } finally {
            closeIOQuietly(fc);
        }
    }

    /**
     * 通过字符串写入文件
     * @param filePath 文件路径
     * @param content  写入内容
     * @return {@code true} success, {@code false} fail
     */
    public static boolean writeFileFromString(final String filePath, final String content) {
        return writeFileFromString(getFileByPath(filePath), content, false);
    }

    /**
     * 通过字符串写入文件
     * @param filePath 文件路径
     * @param content  写入内容
     * @param append   是否追加到结尾
     * @return {@code true} success, {@code false} fail
     */
    public static boolean writeFileFromString(final String filePath, final String content, final boolean append) {
        return writeFileFromString(getFileByPath(filePath), content, append);
    }

    /**
     * 通过字符串写入文件
     * @param file    文件
     * @param content 写入内容
     * @return {@code true} success, {@code false} fail
     */
    public static boolean writeFileFromString(final File file, final String content) {
        return writeFileFromString(file, content, false);
    }

    /**
     * 通过字符串写入文件
     * @param file    文件
     * @param content 写入内容
     * @param append  是否追加到结尾
     * @return {@code true} success, {@code false} fail
     */
    public static boolean writeFileFromString(final File file, final String content, final boolean append) {
        if (file == null || content == null) return false;
        if (!createOrExistsFile(file)) return false;
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(file, append));
            bw.write(content);
            return true;
        } catch (IOException e) {
            JCLogUtils.eTag(TAG, e, "writeFileFromString");
            return false;
        } finally {
            closeIOQuietly(bw);
        }
    }

    // ==============
    // = 读写分界线 =
    // ==============

    /**
     * 读取文件内容, 返回换行 List
     * @param filePath 文件路径
     * @return 换行{@link List<String>}
     */
    public static List<String> readFileToList(final String filePath) {
        return readFileToList(getFileByPath(filePath), null);
    }

    /**
     * 读取文件内容, 返回换行 List
     * @param filePath    文件路径
     * @param charsetName 字符编码
     * @return 换行{@link List<String>}
     */
    public static List<String> readFileToList(final String filePath, final String charsetName) {
        return readFileToList(getFileByPath(filePath), charsetName);
    }

    /**
     * 读取文件内容, 返回换行 List
     * @param file 文件
     * @return 换行{@link List<String>}
     */
    public static List<String> readFileToList(final File file) {
        return readFileToList(file, 0, 0x7FFFFFFF, null);
    }

    /**
     * 读取文件内容, 返回换行 List
     * @param file        文件
     * @param charsetName 字符编码
     * @return 换行{@link List<String>}
     */
    public static List<String> readFileToList(final File file, final String charsetName) {
        return readFileToList(file, 0, 0x7FFFFFFF, charsetName);
    }

    /**
     * 读取文件内容, 返回换行 List
     * @param filePath 文件路径
     * @param start    开始位置
     * @param end      结束位置
     * @return 换行{@link List<String>}
     */
    public static List<String> readFileToList(final String filePath, final int start, final int end) {
        return readFileToList(getFileByPath(filePath), start, end, null);
    }

    /**
     * 读取文件内容, 返回换行 List
     * @param filePath    文件路径
     * @param start       开始位置
     * @param end         结束位置
     * @param charsetName 字符编码
     * @return 换行{@link List<String>}
     */
    public static List<String> readFileToList(final String filePath, final int start, final int end, final String charsetName) {
        return readFileToList(getFileByPath(filePath), start, end, charsetName);
    }

    /**
     * 读取文件内容, 返回换行 List
     * @param file  文件
     * @param start 开始位置
     * @param end   结束位置
     * @return 换行{@link List<String>}
     */
    public static List<String> readFileToList(final File file, final int start, final int end) {
        return readFileToList(file, start, end, null);
    }

    /**
     * 读取文件内容, 返回换行 List
     * @param file        文件
     * @param start       开始位置
     * @param end         结束位置
     * @param charsetName 字符编码
     * @return 换行{@link List<String>}
     */
    public static List<String> readFileToList(final File file, final int start, final int end, final String charsetName) {
        if (!isFileExists(file)) return null;
        if (start > end) return null;
        BufferedReader br = null;
        try {
            String line;
            int curLine = 1;
            List<String> list = new ArrayList<>();
            if (isSpace(charsetName)) {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            } else {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(file), charsetName));
            }
            while ((line = br.readLine()) != null) {
                if (curLine > end) break;
                if (start <= curLine && curLine <= end) list.add(line);
                ++curLine;
            }
            return list;
        } catch (IOException e) {
            JCLogUtils.eTag(TAG, e, "readFileToList");
            return null;
        } finally {
            closeIOQuietly(br);
        }
    }

    // =

    /**
     * 读取文件内容, 返回字符串
     * @param filePath 文件路径
     * @return 文件内容字符串
     */
    public static String readFileToString(final String filePath) {
        return readFileToString(getFileByPath(filePath), null);
    }

    /**
     * 读取文件内容, 返回字符串
     * @param filePath    文件路径
     * @param charsetName 字符编码
     * @return 文件内容字符串
     */
    public static String readFileToString(final String filePath, final String charsetName) {
        return readFileToString(getFileByPath(filePath), charsetName);
    }

    /**
     * 读取文件内容, 返回字符串
     * @param file 文件
     * @return 文件内容字符串
     */
    public static String readFileToString(final File file) {
        return readFileToString(file, null);
    }

    /**
     * 读取文件内容, 返回字符串
     * @param file        文件
     * @param charsetName 字符编码
     * @return 文件内容字符串
     */
    public static String readFileToString(final File file, final String charsetName) {
        if (!isFileExists(file)) return null;
        BufferedReader br = null;
        try {
            StringBuilder builder = new StringBuilder();
            if (isSpace(charsetName)) {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            } else {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(file), charsetName));
            }
            String line;
            if ((line = br.readLine()) != null) {
                builder.append(line);
                while ((line = br.readLine()) != null) {
                    builder.append(NEW_LINE_STR).append(line);
                }
            }
            return builder.toString();
        } catch (IOException e) {
            JCLogUtils.eTag(TAG, e, "readFileToString");
            return null;
        } finally {
            closeIOQuietly(br);
        }
    }

    /**
     * 读取文件内容, 返回 byte[]
     * @param filePath 文件路径
     * @return 文件内容 byte[]
     */
    public static byte[] readFileToBytesByStream(final String filePath) {
        return readFileToBytesByStream(getFileByPath(filePath));
    }

    /**
     * 读取文件内容, 返回 byte[]
     * @param file 文件
     * @return 文件内容 byte[]
     */
    public static byte[] readFileToBytesByStream(final File file) {
        if (!isFileExists(file)) return null;
        FileInputStream fis = null;
        ByteArrayOutputStream baos = null;
        try {
            fis = new FileInputStream(file);
            baos = new ByteArrayOutputStream();
            byte[] b = new byte[sBufferSize];
            int len;
            while ((len = fis.read(b, 0, sBufferSize)) != -1) {
                baos.write(b, 0, len);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            JCLogUtils.eTag(TAG, e, "readFileToBytesByStream");
            return null;
        } finally {
            closeIOQuietly(fis, baos);
        }
    }

    /**
     * 通过 FileChannel, 读取文件内容, 返回 byte[]
     * @param filePath 文件路径
     * @return 文件内容 byte[]
     */
    public static byte[] readFileToBytesByChannel(final String filePath) {
        return readFileToBytesByChannel(getFileByPath(filePath));
    }

    /**
     * 通过 FileChannel, 读取文件内容, 返回 byte[]
     * @param file 文件
     * @return 文件内容 byte[]
     */
    public static byte[] readFileToBytesByChannel(final File file) {
        if (!isFileExists(file)) return null;
        FileChannel fc = null;
        try {
            fc = new RandomAccessFile(file, "r").getChannel();
            ByteBuffer byteBuffer = ByteBuffer.allocate((int) fc.size());
            while (true) {
                if (!((fc.read(byteBuffer)) > 0)) break;
            }
            return byteBuffer.array();
        } catch (IOException e) {
            JCLogUtils.eTag(TAG, e, "readFileToBytesByChannel");
            return null;
        } finally {
            closeIOQuietly(fc);
        }
    }

    /**
     * 通过 MappedByteBuffer, 读取文件内容, 返回 byte[]
     * @param filePath 文件路径
     * @return 文件内容 byte[]
     */
    public static byte[] readFileToBytesByMap(final String filePath) {
        return readFileToBytesByMap(getFileByPath(filePath));
    }

    /**
     * 通过 MappedByteBuffer, 读取文件内容, 返回 byte[]
     * @param file 文件
     * @return 文件内容 byte[]
     */
    public static byte[] readFileToBytesByMap(final File file) {
        if (!isFileExists(file)) return null;
        FileChannel fc = null;
        try {
            fc = new RandomAccessFile(file, "r").getChannel();
            int size = (int) fc.size();
            MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY, 0, size).load();
            byte[] result = new byte[size];
            mbb.get(result, 0, size);
            return result;
        } catch (IOException e) {
            JCLogUtils.eTag(TAG, e, "readFileToBytesByMap");
            return null;
        } finally {
            closeIOQuietly(fc);
        }
    }

    // ======================
    // = 其他工具类实现代码 =
    // ======================

    // ==============
    // = CloseUtils =
    // ==============

    /**
     * 安静关闭 IO
     * @param closeables Closeable[]
     */
    private static void closeIOQuietly(final Closeable... closeables) {
        if (closeables == null) return;
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    // ==============
    // = FileUtils =
    // ==============

    /**
     * 获取文件
     * @param filePath 文件路径
     * @return 文件 {@link File}
     */
    private static File getFileByPath(final String filePath) {
        return filePath != null ? new File(filePath) : null;
    }

    /**
     * 判断文件是否存在, 不存在则判断是否创建成功
     * @param filePath 文件路径
     * @return {@code true} 存在或创建成功, {@code false} 不存在或创建失败
     */
    private static boolean createOrExistsFile(final String filePath) {
        return createOrExistsFile(getFileByPath(filePath));
    }

    /**
     * 判断文件是否存在, 不存在则判断是否创建成功
     * @param file 文件
     * @return {@code true} 存在或创建成功, {@code false} 不存在或创建失败
     */
    private static boolean createOrExistsFile(final File file) {
        if (file == null) return false;
        // 如果存在, 是文件则返回 true, 是目录则返回 false
        if (file.exists()) return file.isFile();
        // 判断文件是否存在, 不存在则直接返回
        if (!createOrExistsDir(file.getParentFile())) return false;
        try {
            // 存在, 则返回新的路径
            return file.createNewFile();
        } catch (Exception e) {
            JCLogUtils.eTag(TAG, e, "createOrExistsFile");
            return false;
        }
    }

    /**
     * 判断目录是否存在, 不存在则判断是否创建成功
     * @param file 文件
     * @return {@code true} 存在或创建成功, {@code false} 不存在或创建失败
     */
    private static boolean createOrExistsDir(final File file) {
        // 如果存在, 是目录则返回 true, 是文件则返回 false, 不存在则返回是否创建成功
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }

    /**
     * 检查是否存在某个文件
     * @param file 文件路径
     * @return {@code true} yes, {@code false} no
     */
    private static boolean isFileExists(final File file) {
        return file != null && file.exists();
    }

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
