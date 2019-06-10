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

package me.panpf.tool4j.util.zip;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import me.panpf.tool4j.io.FileUtils;
import me.panpf.tool4j.io.IOUtils;

/**
 * ZIP文件工具类
 */
public class ZipUtils {
    /**
     * 将给定的目录下所有的文件打成ZIP包，存到给定的文件中
     *
     * @param directory 给定的目录
     * @param saveFile  给定的文件
     * @throws java.io.IOException
     */
    public static void compression(File directory, File saveFile) throws IOException {
        //如果给定的目录不存在
        if (!directory.exists()) {
            throw new FileNotFoundException("directory : " + directory.getPath() + " can't find");
        }
        //如果给定的目录不是目录
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException(directory.getPath() + " not directory");
        }
        //如果给定的文件不存在
        if (!saveFile.exists()) {
            throw new FileNotFoundException("file : " + saveFile.getPath() + " can't find");
        }

        //获取给定的目录下的所有文件
        File[] files = directory.listFiles();
        //如果文件个数大于0
        if (files.length > 0) {
            //打开ZIP输出流
            ZipOutputStream zipOutputStream = new ZipOutputStream(IOUtils.openBufferedOutputStream(saveFile, false));
            try {
                //遍历文件数组，输出所有文件
                for (File file : files) {
                    putEntry(file, zipOutputStream, "");
                }
                //关闭ZIP输出流
                zipOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                zipOutputStream.close();
                throw e;
            }
        }
    }

    /**
     * 将给定的文件列表中所有的文件打成ZIP包，存到给定的文件中
     *
     * @param fileList 给定的文件列表
     * @param saveFile 给定的文件
     * @throws java.io.IOException
     */
    public static void compression(List<File> fileList, File saveFile) throws IOException {
        //如果给定的文件不存在
        if (!saveFile.exists()) {
            throw new FileNotFoundException("file : " + saveFile.getPath() + " can't find");
        }

        //如果文件个数大于0
        if (fileList.size() > 0) {
            //打开ZIP输出流
            ZipOutputStream zipOutputStream = new ZipOutputStream(IOUtils.openBufferedOutputStream(saveFile, false));
            try {
                //遍历文件按数组，输出所有文件
                for (File file : fileList) {
                    putEntry(file, zipOutputStream, "");
                }
                //关闭ZIP输出流
                zipOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                zipOutputStream.close();
                throw e;
            }
        }
    }

    /**
     * 将给定的文件数组中所有的文件打成ZIP包，存到给定的文件中
     *
     * @param saveFile 给定的文件
     * @param files    给定的文件数组
     * @throws java.io.IOException
     */
    public static void compression(File saveFile, File... files) throws IOException {
        //如果给定的文件不存在
        if (!saveFile.exists()) {
            throw new FileNotFoundException("file : " + saveFile.getPath() + " can't find");
        }

        //如果文件个数大于0
        if (files.length > 0) {
            //打开ZIP输出流
            ZipOutputStream zipOutputStream = new ZipOutputStream(IOUtils.openBufferedOutputStream(saveFile, false));
            try {
                //遍历文件按数组，输出所有文件
                for (File file : files) {
                    putEntry(file, zipOutputStream, "");
                }
                //关闭ZIP输出流
                zipOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                zipOutputStream.close();
                throw e;
            }
        }
    }

    /**
     * 将给定的文件输出到给定的ZIP输出流中
     *
     * @param resFile         给定的文件
     * @param zipOutputStream ZIP输出流
     * @param directoryPath   当前文件的目录路径
     * @throws java.io.FileNotFoundException 文件找不到
     * @throws java.io.IOException
     */
    private static void putEntry(File resFile, ZipOutputStream zipOutputStream, String directoryPath) throws FileNotFoundException, IOException {
        directoryPath = directoryPath + (directoryPath.trim().length() == 0 ? "" : File.separator) + resFile.getName();
        if (resFile.isDirectory()) {
            File[] fileList = resFile.listFiles();
            for (File file : fileList) {
                putEntry(file, zipOutputStream, directoryPath);
            }
        } else {
            InputStream inputStream = IOUtils.openBufferedInputStream(resFile);
            zipOutputStream.putNextEntry(new ZipEntry(directoryPath));
            IOUtils.outputFromInput(inputStream, zipOutputStream);
            inputStream.close();
            zipOutputStream.closeEntry();
        }
    }


    /**
     * 解压给定的ZIP文件到给定的目录中
     *
     * @param zipSourceFile 给定的ZIP文件
     * @param saveDirectory 给定的目录
     * @throws java.util.zip.ZipException
     * @throws java.io.IOException        给定的ZIP文件找不到、给定的目录不存在、给定的目录不是目录
     */
    public static void decompression(File zipSourceFile, File saveDirectory) throws ZipException, IOException {
        //如果ZIP源文件不存在
        if (!zipSourceFile.exists()) {
            throw new FileNotFoundException("file : " + zipSourceFile.getPath() + " can't find");
        }
        //如果保存目录不存在
        if (!saveDirectory.exists()) {
            throw new FileNotFoundException("directory : " + saveDirectory.getPath() + " can't find");
        }
        //如果保存目录不是目录
        if (!saveDirectory.isDirectory()) {
            throw new IllegalArgumentException(saveDirectory.getPath() + " not directory");
        }
        //创建ZIP文件
        ZipFile zipFile = new ZipFile(zipSourceFile);
        try {
            //获取所有的元素
            Enumeration<?> entries = zipFile.entries();
            //获取保存目录的路径
            String saveDirectoryPath = saveDirectory.getPath();
            //循环遍历所有的元素
            while (entries.hasMoreElements()) {
                //获取下一个元素
                ZipEntry zipEntry = (ZipEntry) entries.nextElement();
                //创建要存储当前元素的文件
                File file = new File(saveDirectoryPath + File.separator + zipEntry.getName());
                //如果当前元素是目录
                if (zipEntry.isDirectory()) {
                    //创建目录
                    file.mkdirs();
                } else {
                    //尝试创建文件，如果文件已存在
                    if (!file.createNewFile()) {
                        //将文件的长度设为0
                        FileUtils.setFileLength(file, 0);
                    }
                    //从源ZIP文件中获取当前元素的输入流
                    InputStream inputStream = zipFile.getInputStream(zipEntry);
                    //打开刚创建的文件的输出流
                    OutputStream outputStream = IOUtils.openOutputStream(file, false);
                    try {
                        //从输入流中获取数据写到输出流中
                        IOUtils.outputFromInput(inputStream, outputStream);
                        //关闭输入流
                        outputStream.close();
                        //关闭输出流
                        inputStream.close();
                    } catch (IOException e1) {
                        //关闭输入流
                        outputStream.close();
                        //关闭输出流
                        inputStream.close();
                        throw e1;
                    }
                }
            }
            //关闭ZIP文件
            zipFile.close();
        } catch (ZipException e) {
            //关闭ZIP文件
            zipFile.close();
            throw e;
        } catch (IOException ee) {
            //关闭ZIP文件
            zipFile.close();
            throw ee;
        }
    }
}
