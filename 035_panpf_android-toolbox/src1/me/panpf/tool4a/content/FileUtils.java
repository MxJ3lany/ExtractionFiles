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

package me.panpf.tool4a.content;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;

import me.panpf.tool4a.os.SDCardUtils;

public class FileUtils {
    /**
     * 获取动态文件目录
     *
     * @param context 上下文
     * @return 如果SD卡可用，就返回外部文件目录，否则返回机身自带文件目录
     */
    public static File getDynamicFilesDir(Context context) {
        if (SDCardUtils.isAvailable()) {
            File dir = context.getExternalFilesDir(null);
            if (dir == null) {
                dir = context.getFilesDir();
            }
            return dir;
        } else {
            return context.getFilesDir();
        }
    }

    /**
     * 获取动态获取缓存目录
     *
     * @param context 上下文
     * @return 如果SD卡可用，就返回外部缓存目录，否则返回机身自带缓存目录
     */
    public static File getDynamicCacheDir(Context context) {
        if (SDCardUtils.isAvailable()) {
            File dir = context.getExternalCacheDir();
            if (dir == null) {
                dir = context.getCacheDir();
            }
            return dir;
        } else {
            return context.getCacheDir();
        }
    }

    /**
     * 从文件目录中获取一个文件
     *
     * @param context  上下文
     * @param fileName 要获取的文件的名称
     * @return
     */
    public static File getFileFromFilesDir(Context context, String fileName) {
        return new File(context.getFilesDir().getPath() + File.separator + fileName);
    }

    /**
     * 从外部文件目录中获取一个文件
     *
     * @param context  上下文
     * @param fileName 要获取的文件的名称
     * @return null：SD卡不可用
     */
    public static File getFileFromExternalFilesDir(Context context, String fileName) {
        if (SDCardUtils.isAvailable()) {
            File dir = context.getExternalFilesDir(null);
            if (dir != null) {
                return new File(dir.getPath() + File.separator + fileName);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * 从缓存目录中获取一个文件
     *
     * @param context  上下文
     * @param fileName 要获取的文件的名称
     * @return
     */
    public static File getFileFromCacheDir(Context context, String fileName) {
        return new File(context.getCacheDir().getPath() + File.separator + fileName);
    }

    /**
     * 从外部缓存目录中获取一个文件
     *
     * @param context  上下文
     * @param fileName 要获取的文件的名称
     * @return null：SD卡不可用
     */
    public static File getFileFromExternalCacheDir(Context context, String fileName) {
        if (SDCardUtils.isAvailable()) {
            File dir = context.getExternalCacheDir();
            if (dir != null) {
                return new File(dir.getPath() + File.separator + fileName);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * 从动态文件目录中获取文件
     *
     * @param context  上下文
     * @param fileName 要获取的文件的名称
     * @return 如果SD卡可用，就返回外部文件目录中获取文件，否则从机身自带文件目录中获取文件
     */
    public static File getFileFromDynamicFilesDir(Context context, String fileName) {
        return new File(getDynamicFilesDir(context).getPath() + File.separator + fileName);
    }

    /**
     * 从动态缓存目录中获取文件
     *
     * @param context  上下文
     * @param fileName 要获取的文件的名称
     * @return 如果SD卡可用，就返回外部缓存目录中获取文件，否则从机身自带缓存目录中获取文件
     */
    public static File getFileFromDynamicCacheDir(Context context, String fileName) {
        return new File(getDynamicCacheDir(context).getPath() + File.separator + fileName);
    }

    /**
     * 根据Uri获取路径
     *
     * @param context
     * @param uri
     * @return
     */
    public static String getPathByUri(Context context, Uri uri) {
        String filePath = null;
        Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            cursor.close();
        }
        return filePath;
    }
}