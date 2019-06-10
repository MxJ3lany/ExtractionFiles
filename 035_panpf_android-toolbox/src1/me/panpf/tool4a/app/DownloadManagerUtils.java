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

package me.panpf.tool4a.app;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class DownloadManagerUtils {
    /**
     * 根据请求ID获取本地文件的Uri
     *
     * @return 不存在
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static String getLocalFileUriByRequestId(Context context, long requestId) {
        String result = null;
        if (Build.VERSION.SDK_INT >= 9) {
            Query query = new Query();
            query.setFilterById(requestId);
            Cursor cursor = ((DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE)).query(query);
            if (cursor.moveToFirst()) {
                result = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
            }
            cursor.close();
        }
        return result;
    }

    /**
     * 根据请求ID判断下载是否完成
     *
     * @param context
     * @param requestId
     * @return
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static boolean isFinish(Context context, long requestId) {
        if (Build.VERSION.SDK_INT >= 9) {
            Query query = new Query();
            query.setFilterById(requestId);
            Cursor cursor = ((DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE)).query(query);
            boolean result = cursor.moveToFirst() && cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL;
            cursor.close();
            return result;
        } else {
            return false;
        }
    }

    /**
     * 根据请求ID判断是否正在下载
     *
     * @param context
     * @param requestId
     * @return
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static boolean isDownloading(Context context, long requestId) {
        if (Build.VERSION.SDK_INT >= 9) {
            Query query = new Query();
            query.setFilterById(requestId);
            Cursor cursor = ((DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE)).query(query);
            boolean result = cursor.moveToFirst() && cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_RUNNING;
            cursor.close();
            return result;
        } else {
            return false;
        }
    }
}