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

package me.panpf.tool4j.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import me.panpf.tool4j.lang.StringUtils;
import me.panpf.tool4j.util.CheckingUtils;
import me.panpf.tool4j.util.DateTimeUtils;

/**
 * <b>文件工具类，提供一些有关文件的便捷方法</b>
 * <br>
 * <br><b>1、文件内容的读操作：</b>
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;从给定的文件中读取字节：public static byte[] readByte(File file, long off, int length)
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;从给定的文件中读取字节：public static byte[] readByte(File file, long off)
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;从给定的文件中读取字节：public static byte[] readByte(File file, int length)
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;从给定的文件中读取字节：public static byte[] readByte(File file)
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;从给定的文件中读取字节：public static byte[] readByte(File file)
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;从给定的文件中读取字符：public static char[] readChar(File file, long off, int length)
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;从给定的文件中读取字符：public static char[] readChar(File file, long off)
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;从给定的文件中读取字符：public static char[] readChar(File file, int length)
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;从给定的文件中读取字符：public static char[] readChar(File file)
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;从给定的文件中读取字符并使用给定的字符集编码：public static char[] readChar(File file, long off, int length, Charset charset)
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;从给定的文件中读取字符并使用给定的字符集编码：public static char[] readChar(File file, long off, Charset charset)
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;从给定的文件中读取字符并使用给定的字符集编码：public static char[] readChar(File file, int length, Charset charset)
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;从给定的文件中读取字符并使用给定的字符集编码：public static char[] readChar(File file, Charset charset)
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;从给定的文件中读取字符串：public static String readString(File file)
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;从给定的文件中读取字符串并使用给定的字符集编码：public static String readString(File file, Charset charset)
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;从给定的文件中按每次读取一行的方式读取字符串：public static String[] readStringByLine(File file)
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;从给定的文件中按每次读取一行的方式读取字符串并使用给定的字符集编码：public static String[] readStringByLine(File file, Charset charset)
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;从给定的文件中读取一个对象：public static Object readObject(File file)
 * <br>
 * <br><b>文件内容的随机读操作：</b>
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;从给定的随机访问文件的给定的偏移量处开始读取给定个数的字节：public static byte[] readByte(RandomAccessFile raf, long off, int length)
 * <br>
 * <br><b>文件内容的写操作：</b>
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;把给定的字节数组写到给定的文件中：public static void writeByte(File file, byte[] bytes, long off, int length, boolean isAppend)
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;把给定的字节数组写到给定的文件中：public static void writeByte(File file, byte[] bytes, long off, boolean isAppend)
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;把给定的字节数组写到给定的文件中：public static void writeByte(File file, byte[] bytes, int length, boolean isAppend)
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;把给定的字节数组写到给定的文件中：public static void writeByte(File file, byte[] bytes, boolean isAppend)
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;把给定的字符数组写到给定的文件中：public static void writeChar(File file, char[] chars, long off, int length, boolean isAppend)
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;把给定的字符数组写到给定的文件中：public static void writeChar(File file, char[] chars, long off, boolean isAppend)
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;把给定的字符数组写到给定的文件中：public static void writeChar(File file, char[] chars, int length, boolean isAppend)
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;把给定的字符数组写到给定的文件中：public static void writeChar(File file, char[] chars, boolean isAppend)
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;把给定的字符数组写到给定的文件中并使用给定的字符集编码：public static void writeChar(File file, char[] chars, long off, int length, Charset charset,  boolean isAppend)
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;把给定的字符数组写到给定的文件中并使用给定的字符集编码：public static void writeChar(File file, char[] chars, long off, Charset charset, boolean isAppend)
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;把给定的字符数组写到给定的文件中并使用给定的字符集编码：public static void writeChar(File file, char[] chars, int length, Charset charset,  boolean isAppend)
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;把给定的字符数组写到给定的文件中并使用给定的字符集编码：public static void writeChar(File file, char[] chars, Charset charset, boolean isAppend)
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;把给定的字符串写到给定的文件中：public static void writeString(File file, String string, boolean isAppend)
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;把给定的字符串写到给定的文件中并使用给定的字符集编码：public static void writeString(File file, String string, Charset charset, boolean isAppend)
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;把给定的字符串写到给定的文件中并且在写完之后加上换行：public static void writeStringByLine(File file, String string, boolean isAppend)
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;把给定的字符串写到给定的文件中并且在写完之后加上换行以及使用给定的字符集编码：public static void writeStringByLine(File file, String string, Charset charset, boolean isAppend)
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;把给定的对象写到给定的文件中：public static void writeObject(File file, Object object)
 * <br>
 * <br><b>文件内容的删除、替换与插入操作：</b>
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;使用切割的方式来删除给定文件中的一段数据：public static void removeFileDataByCutWay(File file, long off, long length) throws IOException, IllegalArgumentExceptionn{
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;使用创建新文件的方式来删除给定文件中的一段数据：public static void removeFileDataByNewFileWay(File file, long off, long length) throws IOException{
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;使用切割的方式来替换给定文件中的一段数据：public static void replaceFileDataByCutWay(File file, long off, long length, byte[] newData)
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;使用创建新文件的方式来替换给定文件中的一段数据：public static void replaceFileDataByNewFileWay(File file, long off, long length, byte[] newData)
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;使用切割的方式在给定的文件中给定的位置处插入一段数据：public static void insertFileDataByCutWay(File file, long off, byte[] data)
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;使用创建新文件的方式在给定的文件中给定的位置处插入一段数据：public static void insertFileDataByNewFileWay(File file, long off, byte[] data)
 * <br>
 * <br><b>文件或目录的辅助操作：</b>
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;从给定的文件路径中获取一个文件：public static File getFile(String filePath)
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;创建目录：public static File createDirectory(String directoryPath){
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;创建给定的文件路径表示的文件：public static File createFile(String filePath, boolean isDeleteOldFile)
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;在给定的目录下创建一个给定名称的文件：public static File createFile(File directory, String fileName, boolean isDeleteOldFile)
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;在给定的目录下创建一文件，文件的名字为当前的日期时间，后缀名为tmp：public static File createFileByCurrentDateTime(File directory)
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;删除给定的文件路径表示的文件表示的文件或目录：public static boolean delete(String filePath){
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;测试给定的文件路径表示的文件或目录是否存在：public static boolean isExists(String filePath){
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;设置给定的文件的长度：public static void setFileLength(File file, int fileLength)
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;检查给定的文件是否属于给定的文件类型集合中的一种：public static boolean checkFileType(File file, String... types){
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;将给定的多个文件按顺序合并，并保存到给定的文件中：public static void fileMerge(File saveFile, FileMergeCallback fileMergeCallback, File... files)
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;计算给定的多个文件的长度：public static long countFileLength(File...files){
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;获取给定的文件名的后缀：public static String getSuffixByFileName(String fileName){
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;获取给定的文件名的前缀：public static String getPrefixByFileName(String fileName){
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;获取给定的文件名的前缀和后缀：public static String[] getPrefixAndSuffixByFileName(String fileName){
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;获取给定的文件的文件名的后缀：public static String getFileNameSuffixByFile(File file){
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;获取给定的文件的文件名的前缀：public static String getFileNamePrefixByFile(File file){
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;获取给定的文件的文件名的前缀和后缀：public static String[] getFileNamePrefixAndSuffixByFile(File file){
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;从给定的文件路径中获取文件名：public static String getFileNameByFilePath(String filePath){
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;创建一个只要文件的文件过滤器：public static FileFilter createOnlyFileFileFilter(){
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;创建一个只要目录的文件过滤器：public static FileFilter createOnlyDirFileFilter(){
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;创建一个只要给定的类型的文件的文件过滤器：public static FileFilter createOnlyFileTypeFileFilter(final String...types){
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;创建一个只要目录或给定的类型的文件的过滤器：public static FileFilter createOnlyFileTypeOrDirFileFilter(final String...types){
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;根据文件名对给定的文件数组进行升序排序：public static void sortAscByName(File[] files){
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;根据文件名对给定的文件数组进行降序排序：public static void sortDescByName(File[] files){
 * <br>
 */
public class FileUtils {

    /**
     * 根据文件名字升序排序
     */
    public static FileSortAscByName fileSortAscByName = new FileSortAscByName();
    /**
     * 根据文件名字降序排序
     */
    public static FileSortDescByName fileSortDescByName = new FileSortDescByName();

	
	/* *****************************************************文件内容的读操作******************************************************************* */

    /**
     * 从给定的文件中读取字节
     *
     * @param file   给定的文件
     * @param off    偏移量，从此处开始读取字节
     * @param length 要读取的字节的长度
     * @return 一个字节数组，其长度可能小于length
     * @throws java.io.IOException
     */
    public static byte[] readByte(File file, long off, int length) throws IOException {
        return IOUtils.read(IOUtils.openInputStream(file), off, length);
    }

    /**
     * 从给定的文件中读取字节
     *
     * @param file 给定的文件
     * @param off  偏移量，从此处开始读取字节
     * @return 一个字节数组，其长度可能小于length
     * @throws java.io.IOException
     */
    public static byte[] readByte(File file, long off) throws IOException {
        return IOUtils.read(IOUtils.openInputStream(file), off);
    }

    /**
     * 从给定的文件中读取字节
     *
     * @param file   给定的文件
     * @param length 要读取的字节的长度
     * @return 一个字节数组，其长度可能小于length
     * @throws java.io.IOException
     */
    public static byte[] readByte(File file, int length) throws IOException {
        return IOUtils.read(IOUtils.openInputStream(file), length);
    }

    /**
     * 从给定的文件中读取字节
     *
     * @param file 给定的文件
     * @return 全部字节
     * @throws java.io.IOException
     */
    public static byte[] readByte(File file) throws IOException {
        return IOUtils.read(IOUtils.openInputStream(file));
    }

    /**
     * 从给定的文件中读取字符
     *
     * @param file   给定的文件
     * @param off    偏移量，从此处开始读取字符
     * @param length 要读取的字符的长度
     * @return 一个字符数组，其长度可能小于length
     * @throws java.io.IOException
     */
    public static char[] readChar(File file, long off, int length) throws IOException {
        return IOUtils.read(IOUtils.openReader(file), off, length);
    }

    /**
     * 从给定的文件中读取字符
     *
     * @param file 给定的文件
     * @param off  偏移量，从此处开始读取字符
     * @return 一个字符数组，其长度可能小于length
     * @throws java.io.IOException
     */
    public static char[] readChar(File file, long off) throws IOException {
        return IOUtils.read(IOUtils.openReader(file), off);
    }

    /**
     * 从给定的文件中读取字符
     *
     * @param file   给定的文件
     * @param length 要读取的字符的长度
     * @return 一个字符数组，其长度可能小于length
     * @throws java.io.IOException
     */
    public static char[] readChar(File file, int length) throws IOException {
        return IOUtils.read(IOUtils.openReader(file), length);
    }

    /**
     * 从给定的文件中读取字符
     *
     * @param file 给定的文件
     * @return 全部字符
     * @throws java.io.IOException
     */
    public static char[] readChar(File file) throws IOException {
        return IOUtils.read(IOUtils.openReader(file));
    }

    /**
     * 从给定的文件中读取字符并使用给定的字符集编码
     *
     * @param file    给定的文件
     * @param off     偏移量，从此处开始读取字符
     * @param length  要读取的字符的长度
     * @param charset 给定的字符集
     * @return 一个字符数组，其长度可能小于length
     * @throws java.io.IOException
     */
    public static char[] readChar(File file, long off, int length, Charset charset) throws IOException {
        return IOUtils.read(IOUtils.openReader(file, charset), off, length);
    }

    /**
     * 从给定的文件中读取字符并使用给定的字符集编码
     *
     * @param file    给定的文件
     * @param off     偏移量，从此处开始读取字符
     * @param charset 给定的字符集
     * @return 一个字符数组，其长度可能小于length
     * @throws java.io.IOException
     */
    public static char[] readChar(File file, long off, Charset charset) throws IOException {
        return IOUtils.read(IOUtils.openReader(file, charset), off);
    }

    /**
     * 从给定的文件中读取字符并使用给定的字符集编码
     *
     * @param file    给定的文件
     * @param length  要读取的字符的长度
     * @param charset 给定的字符集
     * @return 一个字符数组，其长度可能小于length
     * @throws java.io.IOException
     */
    public static char[] readChar(File file, int length, Charset charset) throws IOException {
        return IOUtils.read(IOUtils.openReader(file, charset), length);
    }

    /**
     * 从给定的文件中读取字符并使用给定的字符集编码
     *
     * @param file    给定的文件
     * @param charset 给定的字符集
     * @return 全部字符
     * @throws java.io.IOException
     */
    public static char[] readChar(File file, Charset charset) throws IOException {
        return IOUtils.read(IOUtils.openReader(file, charset));
    }

    /**
     * 从给定的文件中读取字符串
     *
     * @param file 给定的文件
     * @return 字符串
     * @throws java.io.IOException
     */
    public static String readString(File file) throws IOException {
        return new String(readChar(file));
    }

    /**
     * 从给定的文件中读取字符串并使用给定的字符集编码
     *
     * @param file    给定的文件
     * @param charset 给定的字符集
     * @return 字符串
     * @throws java.io.IOException
     */
    public static String readString(File file, Charset charset) throws IOException {
        return new String(readChar(file, charset));
    }

    /**
     * 从给定的文件中按每次读取一行的方式读取字符
     *
     * @param file 给定的文件
     * @return String数组
     * @throws java.io.IOException
     */
    public static String[] readStringByLine(File file) throws IOException {
        BufferedReader br = IOUtils.openBufferedReader(file);
        List<String> listString = new ArrayList<String>();
        String string = null;
        while ((string = br.readLine()) != null) {
            listString.add(string);
        }
        br.close();
        return (String[]) listString.toArray();
    }

    /**
     * 从给定的文件中按每次读取一行的方式读取字符并使用给定的字符集编码
     *
     * @param file    给定的文件
     * @param charset 给定的字符集
     * @return String数组
     * @throws java.io.IOException
     */
    public static String[] readStringByLine(File file, Charset charset) throws IOException {
        BufferedReader br = IOUtils.openBufferedReader(file, charset);
        List<String> listString = new ArrayList<String>();
        String string = null;
        while ((string = br.readLine()) != null) {
            listString.add(string);
        }
        br.close();
        return (String[]) listString.toArray();
    }

    /**
     * 从给定的文件中读取一个对象
     *
     * @param file 给定的文件
     * @return 对象
     * @throws java.io.IOException
     * @throws ClassNotFoundException 当文件内的数据不是一个序列化的对象时抛出此异常
     */
    public static Object readObject(File file) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(IOUtils.openBufferedInputStream(file));
        Object object = ois.readObject();
        ois.close();
        return object;
    }

	
	
	/* *****************************************************文件内容的随机读操作******************************************************************* */

    /**
     * 从给定的随机访问文件的给定的偏移量处开始读取给定个数的字节
     *
     * @param raf    给定的随机访问文件，读取完之后文件不会关闭，指针也会复原
     * @param off    给定的偏移量
     * @param length 最多要读取的字节个数
     * @return 一个字节数组
     * @throws java.io.IOException
     */
    public static byte[] readByte(RandomAccessFile raf, long off, int length) throws IOException {
        byte[] result;
        long lastPointer = raf.getFilePointer();
        raf.seek(off);
        byte[] bytes = new byte[length];
        int number = raf.read(bytes);
        raf.seek(lastPointer);
        if (number == length) {
            result = bytes;
        } else {
            result = new byte[number];
            System.arraycopy(bytes, 0, result, 0, number);
        }
        return result;
    }
	
	/* *****************************************************文件内容的写操作******************************************************************* */

    /**
     * 把给定的字节数组写到给定的文件中
     *
     * @param file     给定的文件中
     * @param bytes    给定的字节数组
     * @param off      偏移量，从字节数组的此处开始获取数据写到文件中去
     * @param length   要写出的字节数
     * @param isAppend 是否追加到文件末尾
     * @throws java.io.IOException
     */
    public static void writeByte(File file, byte[] bytes, long off, int length, boolean isAppend) throws IOException {
        OutputStream bos = IOUtils.openOutputStream(file, isAppend);
        IOUtils.write(bos, bytes, off, length);
        bos.flush();
        bos.close();
    }

    /**
     * 把给定的字节数组写到给定的文件中
     *
     * @param file     给定的文件中
     * @param bytes    给定的字节数组
     * @param off      偏移量，从字节数组的此处开始获取数据写到文件中去
     * @param isAppend 是否追加到文件末尾
     * @throws java.io.IOException
     */
    public static void writeByte(File file, byte[] bytes, long off, boolean isAppend) throws IOException {
        OutputStream bos = IOUtils.openOutputStream(file, isAppend);
        IOUtils.write(bos, bytes, off);
        bos.flush();
        bos.close();
    }

    /**
     * 把给定的字节数组写到给定的文件中
     *
     * @param file     给定的文件中
     * @param bytes    给定的字节数组
     * @param length   要写出的字节数
     * @param isAppend 是否追加到文件末尾
     * @throws java.io.IOException
     */
    public static void writeByte(File file, byte[] bytes, int length, boolean isAppend) throws IOException {
        OutputStream bos = IOUtils.openOutputStream(file, isAppend);
        IOUtils.write(bos, bytes, length);
        bos.flush();
        bos.close();
    }

    /**
     * 把给定的字节数组写到给定的文件中
     *
     * @param file     给定的文件中
     * @param bytes    给定的字节数组
     * @param isAppend 是否追加到文件末尾
     * @throws java.io.IOException
     */
    public static void writeByte(File file, byte[] bytes, boolean isAppend) throws IOException {
        OutputStream bos = IOUtils.openOutputStream(file, isAppend);
        IOUtils.write(bos, bytes);
        bos.flush();
        bos.close();
    }

    /**
     * 把给定的字符数组写到给定的文件中
     *
     * @param file     给定的文件中
     * @param chars    给定的字符数组
     * @param off      偏移量，从字符数组的此处开始获取数据写到文件中去
     * @param length   要写出的字符数
     * @param isAppend 是否追加到文件末尾
     * @throws java.io.IOException
     */
    public static void writeChar(File file, char[] chars, long off, int length, boolean isAppend) throws IOException {
        BufferedWriter bw = IOUtils.openBufferedWriter(file, isAppend);
        IOUtils.write(bw, chars, off, length);
        bw.flush();
        bw.close();
    }

    /**
     * 把给定的字符数组写到给定的文件中
     *
     * @param file     给定的文件中
     * @param chars    给定的字符数组
     * @param off      偏移量，从字符数组的此处开始获取数据写到文件中去
     * @param isAppend 是否追加到文件末尾
     * @throws java.io.IOException
     */
    public static void writeChar(File file, char[] chars, long off, boolean isAppend) throws IOException {
        BufferedWriter bw = IOUtils.openBufferedWriter(file, isAppend);
        IOUtils.write(bw, chars, off);
        bw.flush();
        bw.close();
    }

    /**
     * 把给定的字符数组写到给定的文件中
     *
     * @param file     给定的文件中
     * @param chars    给定的字符数组
     * @param length   要写出的字符数
     * @param isAppend 是否追加到文件末尾
     * @throws java.io.IOException
     */
    public static void writeChar(File file, char[] chars, int length, boolean isAppend) throws IOException {
        BufferedWriter bw = IOUtils.openBufferedWriter(file, isAppend);
        IOUtils.write(bw, chars, length);
        bw.flush();
        bw.close();
    }

    /**
     * 把给定的字符数组写到给定的文件中
     *
     * @param file     给定的文件中
     * @param chars    给定的字符数组
     * @param isAppend 是否追加到文件末尾
     * @throws java.io.IOException
     */
    public static void writeChar(File file, char[] chars, boolean isAppend) throws IOException {
        BufferedWriter bw = IOUtils.openBufferedWriter(file, isAppend);
        IOUtils.write(bw, chars);
        bw.flush();
        bw.close();
    }

    /**
     * 把给定的字符数组写到给定的文件中并使用给定的字符集编码
     *
     * @param file     给定的文件中
     * @param chars    给定的字符数组
     * @param off      偏移量，从字符数组的此处开始获取数据写到文件中去
     * @param length   要写出的字符数
     * @param charset  给定的字符集
     * @param isAppend 是否追加到文件末尾
     * @throws java.io.IOException
     */
    public static void writeChar(File file, char[] chars, long off, int length, Charset charset, boolean isAppend) throws IOException {
        BufferedWriter bw = IOUtils.openBufferedWriter(file, charset, isAppend);
        IOUtils.write(bw, chars, off, length);
        bw.flush();
        bw.close();
    }

    /**
     * 把给定的字符数组写到给定的文件中并使用给定的字符集编码
     *
     * @param file     给定的文件中
     * @param chars    给定的字符数组
     * @param off      偏移量，从字符数组的此处开始获取数据写到文件中去
     * @param charset  给定的字符集
     * @param isAppend 是否追加到文件末尾
     * @throws java.io.IOException
     */
    public static void writeChar(File file, char[] chars, long off, Charset charset, boolean isAppend) throws IOException {
        BufferedWriter bw = IOUtils.openBufferedWriter(file, charset, isAppend);
        IOUtils.write(bw, chars, off);
        bw.flush();
        bw.close();
    }

    /**
     * 把给定的字符数组写到给定的文件中并使用给定的字符集编码
     *
     * @param file     给定的文件中
     * @param chars    给定的字符数组
     * @param length   要写出的字符数
     * @param charset  给定的字符集
     * @param isAppend 是否追加到文件末尾
     * @throws java.io.IOException
     */
    public static void writeChar(File file, char[] chars, int length, Charset charset, boolean isAppend) throws IOException {
        BufferedWriter bw = IOUtils.openBufferedWriter(file, charset, isAppend);
        IOUtils.write(bw, chars, length);
        bw.flush();
        bw.close();
    }

    /**
     * 把给定的字符数组写到给定的文件中并使用给定的字符集编码
     *
     * @param file     给定的文件中
     * @param chars    给定的字符数组
     * @param charset  给定的字符集
     * @param isAppend 是否追加到文件末尾
     * @throws java.io.IOException
     */
    public static void writeChar(File file, char[] chars, Charset charset, boolean isAppend) throws IOException {
        BufferedWriter bw = IOUtils.openBufferedWriter(file, charset, isAppend);
        IOUtils.write(bw, chars);
        bw.flush();
        bw.close();
    }

    /**
     * 把给定的字符串写到给定的文件中
     *
     * @param file     给定的文件
     * @param string   给定的字符串
     * @param isAppend 是否追加到文件末尾
     * @throws java.io.IOException
     */
    public static void writeString(File file, String string, boolean isAppend) throws IOException {
        BufferedWriter bw = IOUtils.openBufferedWriter(file, isAppend);
        bw.write(string);
        bw.flush();
        bw.close();
    }

    /**
     * 把给定的字符串写到给定的文件中并使用给定的字符集编码
     *
     * @param file     给定的文件
     * @param string   给定的字符串
     * @param charset  给定的字符集
     * @param isAppend 是否追加到文件末尾
     * @throws java.io.IOException
     */
    public static void writeString(File file, String string, Charset charset, boolean isAppend) throws IOException {
        BufferedWriter bw = IOUtils.openBufferedWriter(file, charset, isAppend);
        bw.write(string);
        bw.flush();
        bw.close();
    }

    /**
     * 把给定的字符串写到给定的文件中并且在写完之后加上换行
     *
     * @param file     要写入的文件
     * @param string   字符串
     * @param isAppend 是否追加到文件末尾
     * @throws java.io.IOException
     */
    public static void writeStringByLine(File file, String string, boolean isAppend) throws IOException {
        BufferedWriter bw = IOUtils.openBufferedWriter(file, isAppend);
        bw.write(string);
        bw.newLine();
        bw.flush();
        bw.close();
    }

    /**
     * 把给定的字符串写到给定的文件中并且在写完之后加上换行以及使用给定的字符集编码
     *
     * @param file     要写入的文件
     * @param string   字符串
     * @param charset  给定的字符集
     * @param isAppend 是否追加到文件末尾
     * @throws java.io.IOException
     */
    public static void writeStringByLine(File file, String string, Charset charset, boolean isAppend) throws IOException {
        BufferedWriter bw = IOUtils.openBufferedWriter(file, charset, isAppend);
        bw.write(string);
        bw.newLine();
        bw.flush();
        bw.close();
    }

    /**
     * 把给定的对象写到给定的文件中
     *
     * @param file   给定的文件
     * @param object 给定的对象
     * @throws java.io.IOException
     */
    public static void writeObject(File file, Object object) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(IOUtils.openBufferedOutputStream(file, false));
        oos.writeObject(object);
        oos.flush();
        oos.close();
    }
	
	
	/* *****************************************************文件内容的删除、替换与插入操作******************************************************************** */

    /**
     * 使用切割的方式来删除给定文件中一段数据
     *
     * @param file   给定的文件
     * @param off    偏移量，从此处开始删除数据
     * @param length 要删除的数据的长度，大于0
     * @throws java.io.IOException
     * @throws IllegalArgumentException (fileLength - (off + length)) > 31457280 因为本方法采用的是先把需要删除的数据之后的数据读到内存中，然后将文件截短，最后把之前保存的数据写到文件中。因此读到内存中的数据不能太大
     */
    public static void removeFileDataByCutWay(File file, long off, long length) throws IOException, IllegalArgumentException {
        //获取文件长度
        long fileLength = file.length();

        //验证数据合法性
        CheckingUtils.valiLongValue(off, 0, fileLength - 1, "off");
        CheckingUtils.valiLongValue(off + length, off + 1, fileLength, "length");

        //计算需读到内存的数据的长度
        long keepDataLength = fileLength - (off + length);

        //如果需要读到内存的数据长度为0，说明需要删除的数据在文件尾部，直接设置文件长度即可
        if (keepDataLength == 0) {
            //打开原文件
            RandomAccessFile raf = new RandomAccessFile(file, "rw");

            //设置长度
            raf.setLength(off);

            //关闭原文件
            raf.close();
        } else if (keepDataLength <= 31457280) {
            //打开原文件
            RandomAccessFile raf = new RandomAccessFile(file, "rw");

            //读取要保存的数据
            byte[] keepData = new byte[(int) keepDataLength];
            raf.seek(off + length);
            raf.read(keepData);

            //将文件截掉合适的长度
            raf.setLength(fileLength - length);

            //写入保存的数据
            raf.seek(off);
            raf.write(keepData);

            //关闭原文件
            raf.close();
        } else {
            throw new IllegalArgumentException("Need to read the length of data of the memory more than 30720 ((fileLength - (off + length)) > 30720)");
        }
    }

    /**
     * 使用创建新文件的方式来删除给定文件中的一段数据
     *
     * @param file   给定的文件，需要注意的是操作完成后随机访问对象不会被关闭，指针也会还原
     * @param off    偏移量，从此处开始删除数据
     * @param length 要删除的数据的长度，大于0
     * @throws java.io.IOException
     */
    public static void removeFileDataByNewFileWay(File file, long off, long length) throws IOException {
        //获取文件长度
        long fileLength = file.length();

        //验证数据合法性
        CheckingUtils.valiLongValue(off, 0, fileLength - 1, "off");
        CheckingUtils.valiLongValue(off + length, off + 1, fileLength, "length");

        //打开原文件
        RandomAccessFile raf = new RandomAccessFile(file, "rw");

        //创建一新文件
        File newFile = null;
        try {
            newFile = createFileByCurrentDateTime(file.getParentFile());
        } catch (IOException e) {
            raf.close();
            throw e;
        }

        //打开新文件
        RandomAccessFile newRaf = null;
        try {
            newRaf = new RandomAccessFile(newFile, "rw");
        } catch (FileNotFoundException e) {
            raf.close();
            newFile.delete();
            throw e;
        }

        byte[] data = new byte[IOUtils.MAX_OPERATION_LENGTH];

        //读取原文件中之前的数据写入到新文件中去
        raf.seek(0);
        while (raf.getFilePointer() < off) {
            int number = (int) (off - raf.getFilePointer());
            int readNumber = -1;
            if (number >= IOUtils.MAX_OPERATION_LENGTH) {
                readNumber = raf.read(data);
            } else {
                readNumber = raf.read(data, 0, number);
            }
            newRaf.write(data, 0, readNumber);
        }

        //读取原文件中之后的数据写入到新文件中去
        raf.seek(off + length);
        int number = -1;
        while ((number = raf.read(data)) != -1) {
            newRaf.write(data, 0, number);
        }

        //关闭文件
        newRaf.close();
        raf.close();

        //记录原文件的名字并删掉原文件
        String filePath = file.getPath();
        //如果原文件删除失败
        if (!file.delete()) {
            //删除新文件
            newFile.delete();
            throw new IOException();
        }

        //将新文件重命名
        newFile.renameTo(new File(filePath));
    }

    /**
     * 使用切割的方式来替换给定文件中的一段数据
     *
     * @param file    给定的文件
     * @param off     要替换的一段数据的开始位置（包括）
     * @param length  要替换的一段数据的长度，大于1
     * @param newData 用来替换旧数据的新数据
     * @throws java.io.IOException
     * @throws IllegalArgumentException (fileLength - (off + length)) > 31457280 因为本方法采用的是先把需要替换的数据之后的数据读到内存中，然后将文件截短，最后把之前保存的数据写到文件中。因此读到内存中的数据不能太大
     */
    public static void replaceFileDataByCutWay(File file, long off, long length, byte[] newData) throws IOException, IllegalArgumentException {

        //获取文件长度
        long fileLength = file.length();

        //验证数据合法性
        CheckingUtils.valiLongValue(off, 0, fileLength - 1, "off");
        CheckingUtils.valiLongValue(off + length, off + 1, fileLength, "length");
        CheckingUtils.valiObjectIsNull(newData, "newData");

        if (newData.length > 0) {
            //计算需读到内存的数据的长度
            long keepDataLength = fileLength - (off + length);

            //如果需要读到内存的数据长度为0
            if (keepDataLength == 0) {
                //打开原文件
                RandomAccessFile raf = new RandomAccessFile(file, "rw");

                //设置长度
                raf.setLength(off);

                //将新数据写到末尾去
                raf.write(newData);

                //关闭原文件
                raf.close();
            } else if (keepDataLength <= 31457280) {
                //打开原文件
                RandomAccessFile raf = new RandomAccessFile(file, "rw");

                //读取要保存的数据
                byte[] keepData = new byte[(int) keepDataLength];
                raf.seek(off + length);
                raf.read(keepData);

                //将文件截掉合适的长度
                if (length != 0) {
                    raf.setLength(fileLength - length);
                }

                //写入新数据
                raf.seek(off);
                raf.write(newData);

                //写入保存的数据
                raf.write(keepData);

                //关闭原文件
                raf.close();
            } else {
                throw new IllegalArgumentException("Need to read the length of data of the memory more than 30720 ((fileLength - (off + length)) > 30720)");
            }
        }
    }

    /**
     * 使用创建新文件的方式来替换给定文件中的一段数据
     *
     * @param file    给定的文件
     * @param off     要替换的一段数据的开始位置（包括）
     * @param length  要替换的一段数据的长度，大于1
     * @param newData 用来替换旧数据的新数据
     * @throws java.io.IOException
     */
    public static void replaceFileDataByNewFileWay(File file, long off, long length, byte[] newData) throws IOException {
        //获取文件长度
        long fileLength = file.length();

        //验证数据合法性
        CheckingUtils.valiLongValue(off, 0, fileLength - 1, "off");
        CheckingUtils.valiLongValue(off + length, off + 1, fileLength, "length");
        CheckingUtils.valiObjectIsNull(newData, "newData");

        if (newData.length > 0) {
            //打开原文件
            RandomAccessFile raf = new RandomAccessFile(file, "rw");

            //创建一新文件
            File newFile = null;
            try {
                newFile = createFileByCurrentDateTime(file.getParentFile());
            } catch (IOException e) {
                raf.close();
                throw e;
            }

            //打开新文件
            RandomAccessFile newRaf = null;
            try {
                newRaf = new RandomAccessFile(newFile, "rw");
            } catch (FileNotFoundException e) {
                raf.close();
                newFile.delete();
                throw e;
            }

            byte[] data = new byte[IOUtils.MAX_OPERATION_LENGTH];

            //读取原文件中之前的数据写入到新文件中去
            raf.seek(0);
            while (raf.getFilePointer() < off) {
                int number = (int) (off - raf.getFilePointer());
                int readNumber = -1;
                if (number >= IOUtils.MAX_OPERATION_LENGTH) {
                    readNumber = raf.read(data);
                } else {
                    readNumber = raf.read(data, 0, number);
                }
                newRaf.write(data, 0, readNumber);
            }

            //写入新数据
            newRaf.write(newData);

            //读取原文件中之后的数据写入到新文件中去
            raf.seek(off + length);
            int number = -1;
            while ((number = raf.read(data)) != -1) {
                newRaf.write(data, 0, number);
            }

            //关闭文件
            newRaf.close();
            raf.close();

            //记录原文件的名字并删掉原文件
            String filePath = file.getPath();
            //如果原文件删除失败
            if (!file.delete()) {
                //删除新文件
                newFile.delete();
                throw new IOException();
            }

            //将新文件重命名
            newFile.renameTo(new File(filePath));
        }
    }

    /**
     * 使用切割的方式在给定的文件中给定的位置处插入一段数据
     *
     * @param file 给定的文件
     * @param off  在此处开始插入数据最小值为0表示将数据插在文件头部，最大值为文件长度表示将数据插在文件尾部
     * @param data 要插入的数据
     * @throws java.io.IOException
     * @throws IllegalArgumentException (fileLength - (off + length)) > 30720 因为本方法采用的是先把需要替换的数据之后的数据读到内存中，然后将文件截短，最后把之前保存的数据写到文件中。因此读到内存中的数据不能太大
     */
    public static void insertFileDataByCutWay(File file, long off, byte[] data) throws IOException, IllegalArgumentException {

        //获取文件长度
        long fileLength = file.length();

        //验证数据合法性
        CheckingUtils.valiLongValue(off, 0, fileLength, "off");
        CheckingUtils.valiObjectIsNull(data, "data");

        if (data.length > 0) {
            //计算需读到内存的数据的长度
            long keepDataLength = fileLength - off;

            //如果需要读到内存的数据长度为0
            if (keepDataLength == 0) {
                writeByte(file, data, true);
            } else if (keepDataLength <= 31457280) {
                //打开原文件
                RandomAccessFile raf = new RandomAccessFile(file, "rw");

                //读取要保存的数据
                byte[] keepData = new byte[(int) keepDataLength];
                raf.seek(off);
                raf.read(keepData);

                //写入新数据
                raf.seek(off);
                raf.write(data);

                //写入保存的数据
                raf.write(keepData);

                //关闭原文件
                raf.close();
            } else {
                throw new IllegalArgumentException("Need to read the length of data of the memory more than 30720 ((fileLength - (off + length)) > 30720)");
            }
        }
    }

    /**
     * 使用创建新文件的方式在给定的文件中给定的位置处插入一段数据
     *
     * @param file 给定的文件
     * @param off  在此处开始插入数据最小值为0表示将数据插在文件头部，最大值为文件长度表示将数据插在文件尾部
     * @param data 要插入的数据
     * @throws java.io.IOException
     */
    public static void insertFileDataByNewFileWay(File file, long off, byte[] data) throws IOException {
        //获取文件长度
        long fileLength = file.length();

        //验证数据合法性
        CheckingUtils.valiLongValue(off, 0, fileLength, "off");
        CheckingUtils.valiObjectIsNull(data, "data");

        if (data.length > 0) {
            //打开原文件
            RandomAccessFile raf = new RandomAccessFile(file, "rw");

            //创建一新文件
            File newFile = null;
            try {
                newFile = createFileByCurrentDateTime(file.getParentFile());
            } catch (IOException e) {
                raf.close();
                throw e;
            }

            //打开新文件
            RandomAccessFile newRaf = null;
            try {
                newRaf = new RandomAccessFile(newFile, "rw");
            } catch (FileNotFoundException e) {
                raf.close();
                newFile.delete();
                throw e;
            }

            byte[] tempData = new byte[IOUtils.MAX_OPERATION_LENGTH];

            //读取原文件中之前的数据写入到新文件中去
            raf.seek(0);
            while (raf.getFilePointer() < off) {
                int readNumber = -1;
                int number = (int) (off - raf.getFilePointer());
                if (number >= IOUtils.MAX_OPERATION_LENGTH) {
                    readNumber = raf.read(tempData);
                } else {
                    readNumber = raf.read(tempData, 0, number);
                }
                newRaf.write(tempData, 0, readNumber);
            }

            //写入新数据
            newRaf.write(data);

            //读取原文件中之后的数据写入到新文件中去
            raf.seek(off);
            int number = -1;
            while ((number = raf.read(tempData)) != -1) {
                newRaf.write(tempData, 0, number);
            }

            //关闭文件
            newRaf.close();
            raf.close();

            //记录原文件的名字并删掉原文件
            String filePath = file.getPath();
            //如果原文件删除失败
            if (!file.delete()) {
                //删除新文件
                newFile.delete();
                throw new IOException();
            }

            //将新文件重命名
            newFile.renameTo(new File(filePath));
        }
    }
	
	
	/* *****************************************************文件或目录的辅助操作******************************************************************** */

    /**
     * 获取给定文件路径的文件，如果不存在就创建
     *
     * @param filePath 给定的文件路径
     * @return 文件
     * @throws java.io.IOException
     */
    public static File getFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            file.createNewFile();
        }
        return new File(filePath);
    }

    /**
     * 获取给定的目录下给定名称的文件，如果文件不存在就创建
     *
     * @param dir
     * @param fileName
     * @return
     * @throws java.io.FileNotFoundException 给定的目录不存在
     * @throws IllegalArgumentException      给定的dir不是目录
     * @throws java.io.IOException
     */
    public static File getFile(File dir, String fileName) throws FileNotFoundException, IllegalArgumentException, IOException {
        if (dir.exists()) {
            if (dir.isDirectory()) {
                return getFile(dir.getPath() + File.separator + fileName);
            } else {
                throw new IllegalArgumentException();
            }
        } else {
            throw new FileNotFoundException();
        }
    }

    /**
     * 创建目录
     *
     * @param directoryPath 目录的字符串表示形式
     * @return 代表此目录的file对象，仅当创建失败时，返回null
     */
    public static File createDirectory(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists() && !directory.mkdirs()) {
            directory = null;
        }
        return directory;
    }

    /**
     * 获取目录，如果不存在就创建
     *
     * @param directoryPath 目录路径
     * @return 目录
     * @throws IllegalArgumentException 不是目录
     */
    public static File getDirectory(String directoryPath) throws IllegalArgumentException {
        File dir = new File(directoryPath);
        //如果不存在的话就创建
        if (!dir.exists()) {
            dir.mkdirs();
        } else {
            //如果不是目录
            if (!dir.isDirectory()) {
                throw new IllegalArgumentException();
            }
        }
        return dir;
    }

    /**
     * 创建给定的文件路径表示的文件
     *
     * @param filePath        给定的文件路径
     * @param isDeleteOldFile 当给定的文件路径表示的文件已存在时是否删除旧文件
     * @return 刚刚创建的文件，仅当文件已存在但不删除时，返回null
     * @throws java.io.IOException
     */
    public static File createFile(String filePath, boolean isDeleteOldFile) throws IOException {
        File file = new File(filePath);
        boolean isSuccess = file.createNewFile();
        if (!isSuccess) {
            if (isDeleteOldFile) {
                setFileLength(file, 0);
            } else {
                file = null;
            }
        }
        return file;
    }

    /**
     * 创建文件，此方法的重要之处在于，如果其父目录不存在会先创建其父目录
     *
     * @param file
     * @return
     * @throws java.io.IOException
     */
    public static File createFile(File file) throws IOException {
        if (!file.exists()) {
            boolean mkadirsSuccess = true;
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                mkadirsSuccess = parentFile.mkdirs();
            }
            if (mkadirsSuccess) {
                try {
                    file.createNewFile();
                    return file;
                } catch (IOException exception) {
                    exception.printStackTrace();
                    return null;
                }
            } else {
                return null;
            }
        } else {
            return file;
        }
    }

    /**
     * 在给定的目录下创建一个给定名称的文件
     *
     * @param directory       给定的目录
     * @param fileName        给定的文件名
     * @param isDeleteOldFile 当给定的目录下已存在给定名称的文件时是否删除旧文件
     * @return 刚刚创建的文件，仅当文件已存在但不删除时，返回null
     * @throws java.io.IOException
     */
    public static File createFile(File directory, String fileName, boolean isDeleteOldFile) throws IOException {
        return createFile(directory.getPath() + File.separator + fileName, isDeleteOldFile);
    }

    /**
     * 在给定的目录下创建一文件，文件的名字为当前的日期时间，后缀名为tmp
     *
     * @param directory 给定的目录
     * @return 名字为当前的日期时间，后缀名为tmp的文件
     * @throws java.io.IOException
     */
    public static File createFileByCurrentDateTime(File directory) throws IOException {
        File newFile = new File(directory.getPath() + File.separator + DateTimeUtils.getCurrentDateTimeByFormat("yyyyMMddhhmmssSSS") + ".tmp");
        boolean isSuccess = newFile.createNewFile();
        while (!isSuccess) {
            newFile = new File(directory.getPath() + File.separator + DateTimeUtils.getCurrentDateTimeByFormat("yyyyMMddhhmmssSSS") + ".tmp");
            isSuccess = newFile.createNewFile();
        }
        return newFile;
    }

    /**
     * 删除给定的文件，如果当前文件是目录则会删除其包含的所有的文件或目录
     *
     * @param file 给定的文件
     * @return 删除是否成功
     */
    public static boolean delete(File file) {
        if (file == null || !file.exists()) {
            return true;
        }

        boolean result;
        if (file.isFile()) {
            result = file.delete();
        } else {
            File[] files = file.listFiles();
            if (files != null) {
                for (File tempFile : files) {
                    delete(tempFile);
                }
            }
            result = file.delete();
        }
        return result;
    }

    /**
     * 删除给定的路径表示的文件，如果当前文件是目录则会删除其包含的所有的文件或目录
     *
     * @param filePath 给定的路径
     * @return 删除是否成功
     */
    public static boolean delete(String filePath) {
        return delete(new File(filePath));
    }

    /**
     * 删除给定的文件数组中所有的文件
     *
     * @param files 给定的文件数组
     * @return 删除失败的文件列表
     */
    public static List<File> deleteFiles(File... files) {
        if (files == null) {
            return null;
        }

        List<File> fileList = new LinkedList<File>();
        for (File file : files) {
            if (!delete(file)) {
                fileList.add(file);
            }
        }
        return fileList;
    }

    /**
     * 删除给定的文件集合中所有的文件
     *
     * @param collection 给定的文件集合
     * @return 删除失败的文件列表
     */
    public static List<File> deleteFiles(Collection<File> collection) {
        if (collection == null) {
            return null;
        }

        List<File> fileList = new LinkedList<File>();
        for (File file : collection) {
            if (!delete(file)) {
                fileList.add(file);
            }
        }
        return fileList;
    }

    /**
     * 清空给定的目录下所有的文件
     *
     * @param directory 给定的目录
     * @return 删除失败的文件列表；null：给定的目录不存在
     * @throws IllegalArgumentException 给定的目录不是目录
     */
    public static List<File> clearDirectory(File directory) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return null;
        }
        return deleteFiles(directory.listFiles());
    }

    /**
     * 清空给定的目录路径下所有的文件
     *
     * @param directoryPath 给定的目录路径
     * @return 删除失败的文件列表
     * @throws java.io.FileNotFoundException 给定的目录不存在
     * @throws IllegalArgumentException      给定的目录不是目录
     */
    public static List<File> clearDirectory(String directoryPath) throws FileNotFoundException, IllegalArgumentException {
        return clearDirectory(new File(directoryPath));
    }

    /**
     * 删除给定的目下的给定名字的文件
     *
     * @param dir      给定的目录
     * @param fileName 给定名字
     * @return 当且仅当成功删除文件或目录时，返回 true；否则返回 false 如果此路径名表示一个目录，则该目录必须为空才能删除。
     */
    public static boolean delete(File dir, String fileName) {
        if (dir == null || !dir.exists()) {
            return true;
        }
        return delete(new File(dir.getPath() + File.separator + fileName));
    }

    /**
     * 测试给定的文件路径表示的文件或目录是否存在
     *
     * @param filePath 给定的文件路径
     * @return true：存在；false：不存在
     */
    public static boolean isExists(String filePath) {
        return new File(filePath).exists();
    }

    /**
     * 设置给定的文件的长度
     *
     * @param file       给定的文件
     * @param fileLength 文件的长度，以byte为单位
     * @throws java.io.IOException
     */
    public static void setFileLength(File file, long fileLength) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(file, "rwd");
        raf.setLength(fileLength);
        raf.close();
    }

    /**
     * 检查给定的文件是否属于给定的文件类型集合中的一种
     *
     * @param file  给定的文件
     * @param types 给定的文件类型集合，例：String[] types = {".mp3", ".tmp", ".txt"};
     * @return true：属于
     */
    public static boolean checkFileType(File file, String... types) {
        boolean result = false;
        String stringX = file.getName().toLowerCase(Locale.getDefault());
        for (String type : types) {
            if (stringX.endsWith(type)) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * 将给定的多个文件按顺序合并，并保存到给定的文件中
     *
     * @param saveFile 给定的文件
     * @param files    给定的多个文件
     * @throws java.io.IOException
     */
    public static void fileMerge(File saveFile, File... files) throws IOException {
//		int finishSize = 0;																//已完成的数据大小
//		int number = 0;																	//实际读取的数据个数
//		FileInputStream input = null;
//		FileOutputStream output = new FileOutputStream(saveFile);
//		byte[] bytes = new byte[IOUtils.MAX_OPERATION_LENGTH];
//		for(int w = 0; w < files.length; w++){
//			input = new FileInputStream(files[w]);
//			while((number = input.read(bytes)) != -1){
//				output.write(bytes, 0, number);
//				finishSize += number;
//				if(fileMergeCallback != null){
//					fileMergeCallback.onWrite(finishSize);
//				}
//			}
//		}
//		input.close();
//		output.flush();
//		output.close();

        boolean createParentDir = false;
        boolean createNewFile = false;
        boolean isContinue = true;
        File parentDir = null;
        OutputStream outputStream = null;
        InputStream inputStream = null;
        try {
            //如果保存文件不存在
            if (!saveFile.exists()) {
                //如果父目录也不存在
                parentDir = saveFile.getParentFile();
                if (!parentDir.exists()) {
                    createParentDir = parentDir.mkdirs();
                    isContinue = createParentDir;
                }
                if (isContinue) {
                    createNewFile = saveFile.createNewFile();
                    isContinue = createNewFile;
                }
            }
            //如果继续
            if (isContinue) {
                outputStream = IOUtils.openBufferedOutputStream(saveFile, false);
                for (File file : files) {
                    inputStream = IOUtils.openBufferedInputStream(file);
                    IOUtils.outputFromInput(inputStream, outputStream);
                    inputStream.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (createNewFile) {
                saveFile.delete();
            }
            if (createParentDir && parentDir != null && parentDir.exists()) {
                parentDir.delete();
            }
            throw e;
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.flush();
                outputStream.close();
            }
        }
    }

    /**
     * 获取文件长度，此方法的关键点在于，他也能获取目录的长度
     *
     * @param file
     * @return
     */
    public static long countFileLength(File file) {
        long length = 0;
        if (file.isFile()) {
            length += file.length();
        } else {
            File[] files = file.listFiles();
            for (File childFile : files) {
                length += countFileLength(childFile);
            }
        }
        return length;
    }

    /**
     * 计算给定的多个文件的长度，此方法的关键点在于，他也能获取目录的长度
     *
     * @param files 给定的多个文件
     * @return
     */
    public static long countFileLength(File... files) {
        int length = 0;
        for (File file : files) {
            length += countFileLength(file);
        }
        return length;
    }

    /**
     * 获取给定的文件名的后缀
     *
     * @param fileName 给定的文件名
     * @return 后缀
     */
    public static String getSuffixByFileName(String fileName) {
        int pos = fileName.lastIndexOf(".");
        if (pos == -1) {
            return null;
        } else {
            return fileName.substring(pos + 1);
        }
    }

    /**
     * 获取给定的文件名的前缀
     *
     * @param fileName 给定的文件名
     * @return 前缀
     */
    public static String getPrefixByFileName(String fileName) {
        int pos = fileName.lastIndexOf(".");
        if (pos == -1) {
            return fileName;
        } else {
            return fileName.substring(0, pos);
        }
    }

    /**
     * 获取给定的文件名的前缀和后缀
     *
     * @param fileName 给定的文件名
     * @return String[] 长度为2。0：前缀；1：后缀，如果不存在返回null
     */
    public static String[] getPrefixAndSuffixByFileName(String fileName) {
        int pos = fileName.lastIndexOf(".");
        String[] ss = new String[2];
        if (pos == -1) {
            ss[0] = fileName;
            ss[1] = null;
        } else {
            ss[0] = fileName.substring(0, pos);
            ss[1] = fileName.substring(pos + 1);
        }
        return ss;
    }

    /**
     * 获取给定的文件的文件名的后缀
     *
     * @param file 给定的文件
     * @return 后缀，例：mp3、wma， 如果不存在返回null
     */
    public static String getFileNameSuffixByFile(File file) {
        return getSuffixByFileName(file.getName());
    }

    /**
     * 获取给定的文件的文件名的前缀
     *
     * @param file 给定的文件
     * @return 前缀
     */
    public static String getFileNamePrefixByFile(File file) {
        return getPrefixByFileName(file.getName());
    }

    /**
     * 获取给定的文件的文件名的前缀和后缀
     *
     * @param file 给定的文件
     * @return String[] 长度为2。0：前缀；1：后缀，如果不存在返回null
     */
    public static String[] getFileNamePrefixAndSuffixByFile(File file) {
        return getPrefixAndSuffixByFileName(file.getName());
    }

    /**
     * 从给定的文件路径中获取文件名
     *
     * @param filePath 给定的文件路径，例如：f:\Music\MP3百家荟萃\张靓颖\张靓颖 - 如果这就是爱情.mp3
     * @return 文件名，例如：张靓颖 - 如果这就是爱情.mp3
     */
    public static String getFileNameByFilePath(String filePath) {
        return filePath.substring(filePath.lastIndexOf(File.separatorChar) + 1);
    }


    /**
     * 创建一个只要文件的文件过滤器
     *
     * @return FileFilter
     */
    public static FileFilter createOnlyFileFileFilter() {
        return new FileFilter() {
            public boolean accept(File pathname) {
                if (pathname.isFile()) {
                    return true;
                } else {
                    return false;
                }
            }
        };
    }

    /**
     * 创建一个只要目录的文件过滤器
     *
     * @return FileFilter
     */
    public static FileFilter createOnlyDirFileFilter() {
        return new FileFilter() {
            public boolean accept(File pathname) {
                if (pathname.isDirectory()) {
                    return true;
                } else {
                    return false;
                }
            }
        };
    }

    /**
     * 创建一个只要给定的类型的文件的文件过滤器
     *
     * @param types 给定的类型的集合，例：{".rmvb", ".mp3"}
     * @return FileFilter
     */
    public static FileFilter createOnlyFileTypeFileFilter(final String... types) {
        return new FileFilter() {
            public boolean accept(File pathname) {
                if (pathname.isFile() && FileUtils.checkFileType(pathname, types)) {
                    return true;
                } else {
                    return false;
                }
            }
        };
    }

    /**
     * 创建一个只要目录或给定的类型的文件的过滤器
     *
     * @param types 给定的类型的集合，例：{".rmvb", ".mp3"}
     * @return FileFilter
     */
    public static FileFilter createOnlyFileTypeOrDirFileFilter(final String... types) {
        return new FileFilter() {
            public boolean accept(File pathname) {
                if (pathname.isDirectory() || FileUtils.checkFileType(pathname, types)) {
                    return true;
                } else {
                    return false;
                }
            }
        };
    }

    /**
     * 根据文件名对给定的文件数组进行升序排序
     *
     * @param files 给定的文件数组
     */
    public static void sortAscByName(File[] files) {
        Arrays.sort(files, fileSortAscByName);
    }

    /**
     * 根据文件名对给定的文件数组进行降序排序
     *
     * @param files 给定的文件数组
     */
    public static void sortDescByName(File[] files) {
        Arrays.sort(files, fileSortDescByName);
    }

	
	/* *****************************************************内部类******************************************************************** */

    /**
     * 根据文件名字升序排序
     */
    private static class FileSortAscByName implements Comparator<File> {
        @Override
        public int compare(File o1, File o2) {
            int result = 0;
            if (o1.isDirectory() && o2.isDirectory()) {
                result = StringUtils.compare(o1.getName(), o2.getName());
            } else if (o1.isFile() && o2.isFile()) {
                result = StringUtils.compare(o1.getName(), o2.getName());
            } else if (o1.isDirectory() && o2.isFile()) {
                result = -1;
            } else if (o1.isFile() && o2.isDirectory()) {
                result = 1;
            }
            return result;
        }
    }

    /**
     * 根据文件名字降序排序
     */
    private static class FileSortDescByName implements Comparator<File> {
        @Override
        public int compare(File o1, File o2) {
            int result = 0;
            if (o1.isDirectory() && o2.isDirectory()) {
                result = StringUtils.compare(o1.getName(), o2.getName());
            } else if (o1.isFile() && o2.isFile()) {
                result = StringUtils.compare(o1.getName(), o2.getName());
            } else if (o1.isDirectory() && o2.isFile()) {
                result = -1;
            } else if (o1.isFile() && o2.isDirectory()) {
                result = 1;
            }
            return -result;
        }
    }
}