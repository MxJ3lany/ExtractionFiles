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

package me.panpf.tool4a.util;

import android.util.Log;

import java.io.File;
import java.io.IOException;

import me.panpf.tool4j.io.FileUtils;
import me.panpf.tool4j.util.DateTimeUtils;

/**
 * <h2>AndroidLog记录器</h2>
 * <br>*特征1：可以方便的控制是否输出Log。由于我们在开发阶段是需要通过输出Log来进行调试的，而我们的应用在发布到市场以后就不需要输出了。
 * 所以你可以通过AndroidLogger.setEnableOutputToConsole()方法或者AndroidLogger.setEnableOutputToLocalFile()方法来控制是否需要将Log输出到控制台或者输出到本地文件。
 * <br>
 * <br>*特征2：可以将选择将Log输出到本地文件。你可以通过AndroidLogger.setOutputFile()方法来设置存储Log的文件
 * <br>
 * <br>*特征3：你还可以通过设置AndroidLogger.setDefaultLogTag()方法来自定义默认的Log tag
 *
 * @author XIAOPAN
 */
public class ALog {
    /**
     * 默认的Log tag
     */
    private static String logTag = "ALog";
    /**
     * 是否将Log输出到控制台
     */
    private static boolean enable = true;
    /**
     * 是否将Log输出到本地文件
     */
    private static boolean outputToFile = true;
    /**
     * 存储Log的本地文件
     */
    private static File outputFile;

    public static boolean v(String logTag, String logContent) {
        if (enable) {
            Log.v(logTag, logContent);
        }
        return outputToFile(logTag + " " + logContent);
    }

    public static boolean v(String logContent) {
        return v(logTag, logContent);
    }

    public static boolean d(String logTag, String logContent) {
        if (enable) {
            Log.d(logTag, logContent);
        }
        return outputToFile(logTag + " " + logContent);
    }

    public static boolean d(String logContent) {
        return d(logTag, logContent);
    }

    public static boolean i(String logTag, String logContent) {
        if (enable) {
            Log.i(logTag, logContent);
        }
        return outputToFile(logTag + " " + logContent);
    }

    public static boolean i(String logContent) {
        return i(logTag, logContent);
    }

    public static boolean w(String logTag, String logContent) {
        if (enable) {
            Log.w(logTag, logContent);
        }
        return outputToFile(logTag + " " + logContent);
    }

    public static boolean w(String logContent) {
        return w(logTag, logContent);
    }

    public static boolean e(String logTag, String logContent) {
        if (enable) {
            Log.e(logTag, logContent);
        }
        return outputToFile(logTag + " " + logContent);
    }

    public static boolean e(String logContent) {
        return e(logTag, logContent);
    }

    public static boolean wtf(String logTag, String logContent) {
        if (enable) {
            Log.wtf(logTag, logContent);
        }
        return outputToFile(logTag + " " + logContent);
    }

    public static boolean wtf(String logContent) {
        return wtf(logTag, logContent);
    }

    public static boolean wtf(String logTag, Throwable th) {
        if (enable) {
            Log.wtf(logTag, th);
        }
        return outputToFile(logTag + " " + th.getMessage());
    }

    public static boolean wtf(Throwable th) {
        return wtf(logTag, th);
    }

    public static boolean wtf(String logTag, String logContent, Throwable th) {
        if (enable) {
            Log.wtf(logTag, logContent, th);
        }
        return outputToFile(logTag + " " + logContent);
    }

    public static boolean outputToFile(String logContent) {
        if (!outputToFile || outputFile == null) {
            return false;
        }

        if (outputFile.exists()) {
            try {
                FileUtils.writeStringByLine(outputFile, DateTimeUtils.getCurrentDateTimeByDefultFormat() + "" + logContent, true);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            File parentFile = outputFile.getParentFile();
            //如果父目录不存在就创建，如果创建失败了就直接结束
            if (!parentFile.exists() && !parentFile.mkdirs()) {
                return false;
            }
            try {
                if (outputFile.createNewFile()) {
                    FileUtils.writeStringByLine(outputFile, DateTimeUtils.getCurrentDateTimeByDefultFormat() + "" + logContent, true);
                    return true;
                } else {
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    /**
     * 获取Log Tag
     *
     * @return
     */
    public static String getLogTag() {
        return logTag;
    }

    /**
     * 设置Log Tag
     *
     * @param logTag
     */
    public static void setLogTag(String logTag) {
        ALog.logTag = logTag;
    }

    /**
     * 是否可用
     *
     * @return
     */
    public static boolean isEnable() {
        return enable;
    }

    /**
     * 设置是否可用
     *
     * @param enable
     */
    public static void setEnable(boolean enable) {
        ALog.enable = enable;
    }

    /**
     * 是否同时输出log到本地文件
     *
     * @return
     */
    public static boolean isOutputToFile() {
        return outputToFile;
    }

    /**
     * 设置是否同时输出log到本地文件
     *
     * @param outputToFile
     */
    public static void setOutputToFile(boolean outputToFile) {
        ALog.outputToFile = outputToFile;
    }

    /**
     * 获取本地输出文件
     *
     * @return
     */
    public static File getOutputFile() {
        return outputFile;
    }

    /**
     * 设置本地输出文件
     *
     * @param outputFile
     */
    public static void setOutputFile(File outputFile) {
        ALog.outputFile = outputFile;
    }
}
