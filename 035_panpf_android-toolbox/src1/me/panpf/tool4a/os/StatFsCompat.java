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

package me.panpf.tool4a.os;

import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

/**
 * <h4>StatFs兼容类<h4/>
 * <p>
 * 由于StatFs的API从API18之后发生了较大的变化，主要是Android官方认识到之前的API用起来不方便并且有坑，于是新添加了几个方法来替代旧的方法
 */
public class StatFsCompat {
    private StatFs statFs;

    public StatFsCompat(String dir) {
        this.statFs = new StatFs(dir);
    }

    /**
     * 创建一个用于查询外部存储（Environment.getExternalStorageDirectory()）容量的StatFsCompat
     *
     * @return null：外部存储不可用
     */
    @SuppressWarnings("deprecation")
    public static StatFsCompat newByExternalStorage() {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return null;
        }
        return new StatFsCompat(Environment.getExternalStorageDirectory().getPath());
    }

    /**
     * 创建一个用于查询内部存储（/data）容量的StatFsCompat
     *
     * @return null：内部存储不可用
     */
    @SuppressWarnings("deprecation")
    public static StatFsCompat newByInternalStorage() {
        return new StatFsCompat("/data");
    }

    /**
     * 获取可用字节数
     *
     * @return 可用字节数，单位byte
     */
    @SuppressWarnings("deprecation")
    public long getAvailableBytes() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return statFs.getAvailableBytes();
        } else {
            return (long) statFs.getAvailableBlocks() * statFs.getBlockSize();
        }
    }

    /**
     * 获取总字节数
     *
     * @return 总字节数，单位byte
     */
    @SuppressWarnings("deprecation")
    public long getTotalBytes() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return statFs.getTotalBytes();
        } else {
            return (long) statFs.getBlockCount() * statFs.getBlockSize();
        }
    }

    /**
     * 获取空闲字节数
     *
     * @return 空闲字节数，单位byte
     */
    @SuppressWarnings("deprecation")
    public long getFreeBytes() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return statFs.getFreeBytes();
        } else {
            return (long) statFs.getFreeBlocks() * statFs.getBlockSize();
        }
    }
}
