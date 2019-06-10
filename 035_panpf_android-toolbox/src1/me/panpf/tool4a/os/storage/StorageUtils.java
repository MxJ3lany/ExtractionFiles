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

package me.panpf.tool4a.os.storage;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * 存储分析器，用来获取存储相关的内容
 */
public class StorageUtils {

    /**
     * 获取所有SD卡的路径
     *
     * @return 所有SD卡的路径
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static String[] getAllSdcardPath(Context context) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState())) {
                return new String[]{Environment.getExternalStorageDirectory().getPath()};
            } else {
                return null;
            }
        }

        String[] paths;
        Method getVolumePathsMethod;
        try {
            getVolumePathsMethod = StorageManager.class.getMethod("getVolumePaths");
        } catch (NoSuchMethodException e) {
            Log.e("StorageAnalyzer", "当前设备Android版本（" + Build.VERSION.SDK_INT + "）过低StorageManager类中没有getVolumePaths方法，因此不支持多块SD卡");
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                return new String[]{Environment.getExternalStorageDirectory().getPath()};
            } else {
                return null;
            }
        }
        try {
            paths = (String[]) getVolumePathsMethod.invoke(context.getSystemService(Context.STORAGE_SERVICE));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }

        if (paths == null || paths.length == 0) {
            return null;
        }
        return paths;
    }

    /**
     * 获取所有可用的SD卡的路径
     *
     * @return 所有可用的SD卡的路径
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static String[] getAllAvailableSdcardPath(Context context) {
        // 获取所有的存储器的路径
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                return new String[]{Environment.getExternalStorageDirectory().getPath()};
            } else {
                return null;
            }
        }

        String[] paths;
        Method getVolumePathsMethod;
        try {
            getVolumePathsMethod = StorageManager.class.getMethod("getVolumePaths");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
        StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        try {
            paths = (String[]) getVolumePathsMethod.invoke(sm);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }

        if (paths == null || paths.length == 0) {
            return null;
        }

        // 去掉不可用的存储器
        List<String> storagePathList = new LinkedList<String>();
        Collections.addAll(storagePathList, paths);
        Iterator<String> storagePathIterator = storagePathList.iterator();

        String path;
        Method getVolumeStateMethod = null;
        while (storagePathIterator.hasNext()) {
            path = storagePathIterator.next();
            if (getVolumeStateMethod == null) {
                try {
                    getVolumeStateMethod = StorageManager.class.getMethod("getVolumeState", String.class);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            String status;
            try {
                status = (String) getVolumeStateMethod.invoke(sm, path);
            } catch (Exception e) {
                e.printStackTrace();
                storagePathIterator.remove();
                continue;
            }
            if (!(Environment.MEDIA_MOUNTED.equals(status) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(status))) {
                storagePathIterator.remove();
            }
        }
        return storagePathList.toArray(new String[storagePathList.size()]);
    }

    /**
     * 获取默认存储的路径
     *
     * @return 默认存储的路径；null：存储卡不可用
     */
    public static String getDefaultSdcardPath() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return Environment.getExternalStorageDirectory().getPath();
        } else {
            return null;
        }
    }
}
