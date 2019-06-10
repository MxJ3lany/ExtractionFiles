/*
 * Copyright (C) 2018 The JackKnife Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lwh.jackknife.orm;

import android.util.Log;

public class OrmLog {

    private static boolean ORM_DEBUG = true;
    private static String TAG = "jackknife-orm";

    public static void setDebugMode(boolean debugMode) {
        ORM_DEBUG = debugMode;
    }

    public static void i(String msg) {
        if (ORM_DEBUG) {
            Log.i(TAG, msg);
        }
    }

    public static void w(String msg) {
        if (ORM_DEBUG) {
            Log.w(TAG, msg);
        }
    }

    public static void d(String msg) {
        if (ORM_DEBUG) {
            Log.d(TAG, msg);
        }
    }

    public static void e(String msg) {
        if (ORM_DEBUG) {
            Log.e(TAG, msg);
        }
    }

    public static void v(String msg) {
        if (ORM_DEBUG) {
            Log.v(TAG, msg);
        }
    }
}
