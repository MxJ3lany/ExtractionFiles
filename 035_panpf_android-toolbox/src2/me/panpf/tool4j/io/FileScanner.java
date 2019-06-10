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

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import me.panpf.tool4j.lang.StringUtils;
import me.panpf.tool4j.util.StringVerifyMode;


/**
 * 文件扫描器，只要文件
 */
public class FileScanner {
    /**
     * 文件扫描监听器
     */
    private FileScanListener fileScanListener;

    /**
     * 最终的文件过滤器
     */
    private FileFilter finalFileFilter;

    /**
     * 是否深入扫描，默认否
     */
    private boolean deepScan;
    /**
     * 运行
     */
    private boolean scanning;

    /**
     * 文件最大长度
     */
    private long fileMaxLength = -1;

    /**
     * 文件最小长度
     */
    private long fileMinLength = -1;

    /**
     * 文件长度过滤方式
     */
    private FileLengthFilterWayEnum fileLengthFilterWay = FileLengthFilterWayEnum.IN_BETWEEN;

    /**
     * 文件类别过滤方式，默认方式为包含标记
     */
    private StringVerifyMode fileTypeFilterWay = StringVerifyMode.CONTAIN_KEYWORDS;

    /**
     * 文件名过滤方式，默认方式为包含标记
     */
    private StringVerifyMode fileNameFilterWay = StringVerifyMode.CONTAIN_KEYWORDS;
    /**
     * 文件路径过滤方式，默认不等于
     */
    private FilePathFilterWayEnum filePathFilterWay = FilePathFilterWayEnum.NOT_EQUAL;
    /**
     * 是否需要重新创建扩展文件过滤器
     */
    private boolean needResatrtCreateExtendFileFilter = true;

    /**
     * 文件类型关键字列表
     */
    private List<String> fileTypeKeyWordList = new ArrayList<String>();

    /**
     * 文件名关键字列表
     */
    private List<String> fileNameKeyWordList = new ArrayList<String>();

    /**
     * 文件路径列表
     */
    private List<String> filePathKeyWordList = new ArrayList<String>();

    /**
     * 根目录，从此目录开始扫描
     */
    private List<File> rootDirList = new ArrayList<File>();

    /**
     * 文件过滤器列表
     */
    private List<FileFilter> fileFilterList = new ArrayList<FileFilter>();

    /**
     * 扩展文件过滤器列表
     */
    private List<FileFilter> extendFileFilterList = new ArrayList<FileFilter>();

    /**
     * 构造函数，指定根目录
     *
     * @param rootDir 根目录
     */
    public FileScanner(File rootDir) {
        addRootDir(rootDir);
    }

    /**
     * 构造函数，指定多个根目录
     *
     * @param listRootDir 多个根目录
     */
    public FileScanner(List<File> listRootDir) {
        setRootDirList(listRootDir);
    }

    /**
     * 构造函数，指定多个根目录
     *
     * @param rootDirs 多个根目录
     */
    public FileScanner(File... rootDirs) {
        addRootDirs(rootDirs);
    }

    /**
     * 构造函数
     */
    public FileScanner() {
    }

    /**
     * 扫描文件和目录
     *
     * @return 返回包含扫描到的所有的文件和目录的列表
     */
    public List<File> scan() {
        List<File> listFiles = null;

        //扫描开始
        if (fileScanListener != null) {
            fileScanListener.onStart();
        }

        //如果根目录列表不为null并且大小大于0
        if (rootDirList != null && rootDirList.size() > 0) {
            //创建扩展文件过滤器
            createExtendFileFilter();

            //创建最终的文件过滤器
            if (finalFileFilter == null) {
                createFinalFilter();
            }

            //如果深入扫描
            scanning = true;
            if (deepScan) {
                listFiles = deepScan();
            } else {
                listFiles = noDeepScan();
            }
        }

        //扫描结束后，如果是正常停止
        if (isNormalStop()) {
            if (fileScanListener != null) {
                fileScanListener.onFinish(listFiles);
            }
        } else {
            listFiles = null;
            if (fileScanListener != null) {
                fileScanListener.onHumanInterrupt();
            }
        }
        return listFiles;
    }

    /**
     * 深入扫描
     *
     * @return 搜索结果
     */
    private List<File> deepScan() {
        List<File> fileList = new ArrayList<File>();                                        //存放符合条件的文件

        //将搜索目录转移到专门存放目录的列表中去
        Queue<File> dirList = new LinkedBlockingQueue<File>();
        for (File rootDir : rootDirList) {
            dirList.add(rootDir);
        }

        //循环从目录列表中取出目录，并从这个目录中搜索文件
        while (scanning && !dirList.isEmpty()) {
            File currentDir = dirList.poll();
            if (currentDir != null && currentDir.exists() && currentDir.isDirectory()) {
                if (fileScanListener != null) {
                    fileScanListener.onInDirFind(currentDir);
                }

                //获取当前目录下所有的文件
                File[] files = null;
                if (finalFileFilter != null) {
                    files = currentDir.listFiles(finalFileFilter);
                } else {
                    files = currentDir.listFiles();
                }

                //过滤掉文件夹，并将过滤掉的文件夹存放到目录列表中
                if (files != null && files.length > 0) {
                    for (File file : files) {
                        if (scanning) {
                            //如果是文件，就添加到文件列表；否则就添加到目录列表
                            if (file.isFile()) {
                                fileList.add(file);
                                if (fileScanListener != null) {
                                    fileScanListener.onFind(file, fileList.size());
                                }
                            } else {
                                dirList.add(file);
                            }
                        } else {
                            break;
                        }
                    }
                }
            }
        }

        return fileList;
    }

    /**
     * 不深入扫描
     *
     * @return 搜索结果
     */
    private List<File> noDeepScan() {
        List<File> fileList = new ArrayList<File>();                                        //存放符合条件的文件

        //循环遍历所有的目录
        File[] files = null;
        for (File currentDir : rootDirList) {
            if (scanning) {
                if (currentDir != null && currentDir.exists() && currentDir.isDirectory()) {
                    if (fileScanListener != null) {
                        fileScanListener.onInDirFind(currentDir);
                    }

                    //获取当前目录下所有的文件
                    if (finalFileFilter != null) {
                        files = currentDir.listFiles(finalFileFilter);
                    } else {
                        files = currentDir.listFiles();
                    }

                    //过滤掉文件夹
                    if (files != null && files.length > 0) {
                        for (File file : files) {
                            if (scanning) {
                                //如果是文件就添加到文件列表
                                if (file.isFile()) {
                                    fileList.add(file);
                                    if (fileScanListener != null) {
                                        fileScanListener.onFind(file, fileList.size());
                                    }
                                }
                            } else {
                                break;
                            }
                        }
                    }
                }
            } else {
                break;
            }
        }

        return fileList;
    }

    /**
     * 创建扩展文件过滤器
     */
    private void createExtendFileFilter() {
        if (isNeedResatrtCreateExtendFileFilter()) {
            //创建文件长度过滤器
            FileFilter fileLengthFilter = createFileLengthFilter();
            if (fileLengthFilter != null) {
                extendFileFilterList.add(fileLengthFilter);
            }

            //创建文件类别过滤器
            FileFilter fileTypeFilter = createFileTypeFilter();
            if (fileTypeFilter != null) {
                extendFileFilterList.add(fileTypeFilter);
            }

            //创建文件名字过滤器
            FileFilter fileNameFilter = createFileNameFilter();
            if (fileNameFilter != null) {
                extendFileFilterList.add(fileNameFilter);
            }

            //创建文件路径过滤器
            FileFilter filePathFilter = createFilePathFilter();
            if (filePathFilter != null) {
                extendFileFilterList.add(filePathFilter);
            }

            //标记为不需要重新创建
            setNeedResatrtCreateExtendFileFilter(false);
        }
    }

    /**
     * 创建文件路径过滤器
     *
     * @return 文件路径过滤器
     */
    private FileFilter createFilePathFilter() {
        FileFilter filter = null;
        if (getFilePathKeyWordList() != null && getFilePathKeyWordList().size() > 0) {
            filter = new FileFilter() {
                @Override
                public boolean accept(File paramFile) {
                    boolean result = false;
                    if (!filePathKeyWordList.contains(paramFile.getPath())) {
                        result = true;
                    }
                    if (getFilePathFilterWay() == FilePathFilterWayEnum.EQUAL) {
                        result = !result;
                    }
                    return result;
                }
            };
        }
        return filter;
    }

    /**
     * 创建一个验证文件长度的过滤器
     *
     * @return 验证文件长度的过滤器
     */
    private FileFilter createFileLengthFilter() {
        FileFilter filter = null;
        if (fileMinLength > -1 && fileMaxLength <= -1) {
            filter = new FileFilter() {
                @Override
                public boolean accept(File paramFile) {
                    boolean result = false;
                    if (paramFile.length() >= fileMinLength) {
                        result = true;
                    }
                    if (getFileLengthFilterWay() == FileLengthFilterWayEnum.NOT_IN_BETWEEN) {
                        result = !result;
                    }
                    return result;
                }
            };
        } else if (fileMinLength <= -1 && fileMaxLength > -1) {
            filter = new FileFilter() {
                @Override
                public boolean accept(File paramFile) {
                    boolean result = false;
                    if (paramFile.length() <= fileMaxLength) {
                        result = true;
                    }
                    if (getFileLengthFilterWay() == FileLengthFilterWayEnum.NOT_IN_BETWEEN) {
                        result = !result;
                    }
                    return result;
                }
            };
        } else if (fileMinLength > -1 && fileMaxLength > -1) {
            filter = new FileFilter() {
                @Override
                public boolean accept(File paramFile) {
                    boolean result = false;
                    if (paramFile.length() >= fileMinLength && paramFile.length() <= fileMaxLength) {
                        result = true;
                    }
                    if (getFileLengthFilterWay() == FileLengthFilterWayEnum.NOT_IN_BETWEEN) {
                        result = !result;
                    }
                    return result;
                }
            };
        }
        return filter;
    }

    /**
     * 创建一个验证文件类型的过滤器
     *
     * @return 验证文件类型的过滤器
     */
    private FileFilter createFileTypeFilter() {
        FileFilter fileFilter = null;
        if (getFileTypeKeyWordList() != null && getFileTypeKeyWordList().size() > 0) {
            fileFilter = new FileFilter() {
                @Override
                public boolean accept(File paramFile) {
                    boolean result = false;
                    if (paramFile.isFile()) {
                        String houzui = FileUtils.getFileNameSuffixByFile(paramFile);
                        if (houzui != null) {
                            result = StringUtils.checkUp(houzui, getFileTypeKeyWordList(), getFileTypeFilterWay());
                        }
                    } else {
                        result = true;
                    }
                    return result;
                }
            };
        }
        return fileFilter;
    }

    /**
     * 创建一个验证文件名的过滤器
     *
     * @return 验证文件名的过滤器
     */
    private FileFilter createFileNameFilter() {
        FileFilter fileFilter = null;
        if (getFileNameKeyWordList() != null && getFileNameKeyWordList().size() > 0) {
            fileFilter = new FileFilter() {
                @Override
                public boolean accept(File paramFile) {
                    boolean result = false;
                    if (paramFile.isFile()) {
                        String qianzhui = FileUtils.getFileNamePrefixByFile(paramFile);
                        if (qianzhui != null) {
                            result = StringUtils.checkUp(qianzhui, getFileNameKeyWordList(), getFileNameFilterWay());
                        }
                    } else {
                        result = true;
                    }
                    return result;
                }
            };
        }
        return fileFilter;
    }

    /**
     * 创建最终的文件过滤器
     *
     * @return
     */
    private void createFinalFilter() {
        if (fileFilterList.size() > 0 || extendFileFilterList.size() > 0) {
            finalFileFilter = new FileFilter() {
                @Override
                public boolean accept(File paramFile) {
                    boolean result = false;
                    if (paramFile.isFile()) {
                        result = executeFileFilter(paramFile);
                    } else {
                        result = true;
                    }
                    return result;
                }
            };
        } else {
            finalFileFilter = null;
        }
    }

    /**
     * 对给定得到文件执行过滤
     *
     * @param file 被过滤的文件
     * @return true：通过
     */
    private boolean executeFileFilter(File file) {
        boolean filterIsPass = true;
        for (FileFilter ff : extendFileFilterList) {
            if (!ff.accept(file)) {                                //如果不通过
                filterIsPass = false;
                break;                                            //结束循环
            }
        }
        for (FileFilter ff : fileFilterList) {
            if (!ff.accept(file)) {                                //如果不通过
                filterIsPass = false;
                break;                                            //结束循环
            }
        }
        return filterIsPass;
    }

    /**
     * 添加文件路径关键字
     *
     * @param filePaths
     */
    public void addFilePathKeyWords(String... filePaths) {
        for (String filePath : filePaths) {
            if (!filePathKeyWordList.contains(filePath)) {
                filePathKeyWordList.add(filePath);
            }
        }
        //标记为需要重新创建
        setNeedResatrtCreateExtendFileFilter(true);
    }

    /**
     * 添加文件类别关键字
     *
     * @param fileTypeKeyWords 文件类别关键字。如：MP3、txt等
     */
    public void addFileTypeKeyWords(String... fileTypeKeyWords) {
        for (String fileType : fileTypeKeyWords) {
            getFileTypeKeyWordList().add(fileType);
        }
        //标记为需要重新创建
        setNeedResatrtCreateExtendFileFilter(true);
    }

    /**
     * 添加文件类别关键字
     *
     * @param fileNameKeyWords 文件类别关键字。如：MP3、txt等
     */
    public void addFileNameKeyWords(String... fileNameKeyWords) {
        for (String fileName : fileNameKeyWords) {
            getFileNameKeyWordList().add(fileName);
        }
        //标记为需要重新创建
        setNeedResatrtCreateExtendFileFilter(true);
    }

    /**
     * 添加一个文件过滤器
     *
     * @param fileFilter
     */
    public void addFileFilter(FileFilter fileFilter) {
        if (fileFilter != null) {
            fileFilterList.add(fileFilter);
        }
    }

    /**
     * 添加一个根目录
     *
     * @param rootDir 根目录
     */
    public void addRootDir(File rootDir) {
        if (rootDir != null && rootDir.exists() && rootDir.isDirectory()) {
            this.rootDirList.add(rootDir);
        }
    }

    /**
     * 添加多个根目录
     *
     * @param rootDirs 多个根目录
     */
    public void addRootDirs(File... rootDirs) {
        for (File dirFile : rootDirs) {
            addRootDir(dirFile);
        }
    }

    /**
     * 清楚之前所有的设置，需重新指定根目录
     *
     * @param rootDir 根目录
     * @throws IllegalArgumentException
     * @throws java.io.FileNotFoundException
     * @throws NullPointerException
     */
    public void clearAllSetting(File rootDir) throws NullPointerException, FileNotFoundException, IllegalArgumentException {
        getRootDirList().clear();
        getFileFilterList().clear();
        getFileTypeKeyWordList().clear();
        getFileNameKeyWordList().clear();
        getExtendFileFilterList().clear();
        setDeepScan(false);
        setFileScanListener(null);
        setFileMaxLength(-1);
        setFileMinLength(-1);
        setFileLengthFilterWay(FileLengthFilterWayEnum.IN_BETWEEN);
        setFileTypeFilterWay(StringVerifyMode.CONTAIN_KEYWORDS);
        setFileNameFilterWay(StringVerifyMode.CONTAIN_KEYWORDS);
        setNeedResatrtCreateExtendFileFilter(true);
        finalFileFilter = null;
    }

    /**
     * 停止
     */
    public void stop() {
        scanning = false;
    }

    /**
     * 判断是否正常停止
     *
     * @return 是否正常停止
     */
    public boolean isNormalStop() {
        return scanning;
    }

    /**
     * 文件长度过滤方式
     */
    public enum FileLengthFilterWayEnum {
        /**
         * 在两者之间
         */
        IN_BETWEEN,
        /**
         * 不在两者之间
         */
        NOT_IN_BETWEEN;
    }

    /**
     * 文件路径过滤方式
     */
    public enum FilePathFilterWayEnum {
        /**
         * 等于
         */
        EQUAL,
        /**
         * 不不等于
         */
        NOT_EQUAL;
    }

    /**
     * 文件扫描监听器
     */
    public interface FileScanListener {
        /**
         * 当开始时
         */
        public void onStart();

        /**
         * 当找到一个时
         *
         * @param file   本次找到的文件
         * @param number 算上当前这个，已经找到的个数
         */
        public void onFind(File file, int number);

        /**
         * 当在这个目录中搜索的时候
         *
         * @param dir 当前在这个目录中搜索
         */
        public void onInDirFind(File dir);

        /**
         * 当完成时
         *
         * @param fileList 所有找到的文件
         */
        public void onFinish(List<File> fileList);

        /**
         * 当人为中断
         */
        public void onHumanInterrupt();
    }

    /**
     * 获取文件扫描监听器
     *
     * @return 文件扫描监听器
     */
    public FileScanListener getFileScanListener() {
        return fileScanListener;
    }

    /**
     * 设置文件扫描监听器
     *
     * @param fileScanListener 文件扫描监听器
     */
    public void setFileScanListener(FileScanListener fileScanListener) {
        this.fileScanListener = fileScanListener;
    }

    /**
     * 判断是否深入扫描，默认否
     *
     * @return 是否深入扫描，默认否
     */
    public boolean isDeepScan() {
        return deepScan;
    }

    /**
     * 设置是否深入扫描，默认否
     *
     * @param deepScan 是否深入扫描，默认否
     */
    public void setDeepScan(boolean deepScan) {
        this.deepScan = deepScan;
    }

    /**
     * 获取最终的文件过滤器
     *
     * @return 最终的文件过滤器
     */
    public FileFilter getFinalFileFilter() {
        return finalFileFilter;
    }

    /**
     * 设置最终的文件过滤器
     *
     * @param finalFileFilter 最终的文件过滤器
     */
    public void setFinalFileFilter(FileFilter finalFileFilter) {
        this.finalFileFilter = finalFileFilter;
    }

    /**
     * 判断是否正在扫描
     *
     * @return 是否正在扫描
     */
    public boolean isScanning() {
        return scanning;
    }

    /**
     * 设置是否正在扫描
     *
     * @param running 是否正在扫描
     */
    public void setScanning(boolean running) {
        this.scanning = running;
    }

    /**
     * 判断是否需要重新创建扩展处理器
     *
     * @return 是否需要重新创建扩展处理器
     */
    public boolean isNeedResatrtCreateExtendFileFilter() {
        return needResatrtCreateExtendFileFilter;
    }

    /**
     * 设置是否需要重新创建扩展处理器
     *
     * @param needResatrtCreateExtendFileFilter 是否需要重新创建扩展处理器
     */
    public void setNeedResatrtCreateExtendFileFilter(
            boolean needResatrtCreateExtendFileFilter) {
        this.needResatrtCreateExtendFileFilter = needResatrtCreateExtendFileFilter;
    }

    /**
     * 获取根目录
     *
     * @return 根目录
     */
    public List<File> getRootDirList() {
        return rootDirList;
    }

    /**
     * 设置根目录
     *
     * @param rootDir 根目录
     */
    public void setRootDirList(List<File> listRootDir) {
        this.rootDirList = listRootDir;
    }

    /**
     * 获取文件最大长度
     *
     * @return 文件最大长度
     */
    public long getFileMaxLength() {
        return fileMaxLength;
    }

    /**
     * 设置文件最大长度
     *
     * @param fileMaxLength 文件最大长度
     */
    public void setFileMaxLength(long fileMaxLength) {
        this.fileMaxLength = fileMaxLength;
        //标记为需要重新创建
        setNeedResatrtCreateExtendFileFilter(true);
    }

    /**
     * 获取文件最小长度
     *
     * @return 文件最小长度
     */
    public long getFileMinLength() {
        return fileMinLength;
    }

    /**
     * 设置文件最小长度
     *
     * @param fileMinLength 文件最小长度
     */
    public void setFileMinLength(long fileMinLength) {
        this.fileMinLength = fileMinLength;
        //标记为需要重新创建
        setNeedResatrtCreateExtendFileFilter(true);
    }

    /**
     * 获取文件过滤器列表
     *
     * @return 文件过滤器列表
     */
    public List<FileFilter> getFileFilterList() {
        return fileFilterList;
    }

    /**
     * 设置文件过滤器列表
     *
     * @param fileFilterList 文件过滤器列表
     */
    public void setFileFilterList(List<FileFilter> fileFilterList) {
        this.fileFilterList = fileFilterList;
    }

    /**
     * 获取文件类型关键字列表
     *
     * @return 文件类型关键字列表
     */
    public List<String> getFileTypeKeyWordList() {
        return fileTypeKeyWordList;
    }

    /**
     * 设置文件类型关键字列表
     *
     * @param fileTypeKeyWordsList 文件类型关键字列表
     */
    public void setFileTypeKeyWordList(List<String> fileTypeKeyWordsList) {
        this.fileTypeKeyWordList = fileTypeKeyWordsList;
        //标记为需要重新创建
        setNeedResatrtCreateExtendFileFilter(true);
    }

    /**
     * 获取文件名关键字列表
     *
     * @return 文件名关键字列表
     */
    public List<String> getFileNameKeyWordList() {
        return fileNameKeyWordList;
    }

    /**
     * 设置文件名关键字列表
     *
     * @param fileNameKeyWordsList 文件名关键字列表
     */
    public void setFileNameKeyWordList(List<String> fileNameKeyWordsList) {
        this.fileNameKeyWordList = fileNameKeyWordsList;
        //标记为需要重新创建
        setNeedResatrtCreateExtendFileFilter(true);
    }

    public List<FileFilter> getExtendFileFilterList() {
        return extendFileFilterList;
    }

    public void setExtendFileFilterList(List<FileFilter> extendFileFilterList) {
        this.extendFileFilterList = extendFileFilterList;
    }

    /**
     * 获取文件长度过滤方式
     *
     * @return 文件长度过滤方式
     */
    public FileLengthFilterWayEnum getFileLengthFilterWay() {
        return fileLengthFilterWay;
    }

    /**
     * 设置文件长度过滤方式
     *
     * @param fileLengthFilterWay 文件长度过滤方式
     */
    public void setFileLengthFilterWay(FileLengthFilterWayEnum fileLengthFilterWay) {
        this.fileLengthFilterWay = fileLengthFilterWay;
        //标记为需要重新创建
        setNeedResatrtCreateExtendFileFilter(true);
    }

    /**
     * 获取文件类别过滤方式
     *
     * @return 文件类别过滤方式
     */
    public StringVerifyMode getFileTypeFilterWay() {
        return fileTypeFilterWay;
    }

    /**
     * 获取文件路径过滤器
     *
     * @return 文件路径过滤器
     */
    public FilePathFilterWayEnum getFilePathFilterWay() {
        return filePathFilterWay;
    }

    /**
     * 设置文件路径过滤器
     *
     * @param filePathFilterWay 文件路径过滤器
     */
    public void setFilePathFilterWay(FilePathFilterWayEnum filePathFilterWay) {
        this.filePathFilterWay = filePathFilterWay;
    }

    /**
     * 设置文件类别过滤方式
     *
     * @param fileTypeFilterWay
     */
    public void setFileTypeFilterWay(StringVerifyMode fileTypeFilterWay) {
        this.fileTypeFilterWay = fileTypeFilterWay;
        //标记为需要重新创建
        setNeedResatrtCreateExtendFileFilter(true);
    }

    /**
     * 获取文件名过滤方式
     *
     * @return 文件名过滤方式
     */
    public StringVerifyMode getFileNameFilterWay() {
        return fileNameFilterWay;
    }

    /**
     * 设置文件名过滤方式
     *
     * @param fileNameFilterWay 文件名过滤方式
     */
    public void setFileNameFilterWay(StringVerifyMode fileNameFilterWay) {
        this.fileNameFilterWay = fileNameFilterWay;
        //标记为需要重新创建
        setNeedResatrtCreateExtendFileFilter(true);
    }

    /**
     * 获取文件路径关键字列表
     *
     * @return 文件路径关键字列表
     */
    public List<String> getFilePathKeyWordList() {
        return filePathKeyWordList;
    }

    /**
     * 设置文件路径关键字列表
     *
     * @param filePathKeyWordList 文件路径关键字列表
     */
    public void setFilePathKeyWordList(List<String> filePathKeyWordList) {
        this.filePathKeyWordList = filePathKeyWordList;
        //标记为需要重新创建
        setNeedResatrtCreateExtendFileFilter(true);
    }
}