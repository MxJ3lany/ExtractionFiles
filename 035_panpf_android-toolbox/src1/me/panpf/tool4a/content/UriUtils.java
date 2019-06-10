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

import android.net.Uri;

/**
 * URI工具箱
 */
public class UriUtils {
    public static final String URI_TEL = "tel:";
    public static final String URI_SMS = "smsto:";

    /**
     * 获取呼叫给定的电话号码时用的Uri
     *
     * @param phoneNumber 给定的电话号码
     * @return 呼叫给定的电话号码时用的Uri
     */
    public static Uri getCallUri(String phoneNumber) {
        return Uri.parse(URI_TEL + (phoneNumber != null ? phoneNumber : ""));
    }

    /**
     * 获取短信Uri
     *
     * @param mobileNumber 目标手机号
     * @return
     */
    public static Uri getSmsUri(String mobileNumber) {
        return Uri.parse(URI_SMS + (mobileNumber != null ? mobileNumber : ""));
    }
}
