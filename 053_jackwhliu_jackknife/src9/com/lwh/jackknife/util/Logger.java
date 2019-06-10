/*
 * Copyright (C) 2017 The JackKnife Open Source Project
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

package com.lwh.jackknife.util;

import android.util.Log;

/**
 * A system that controls log output globally. When flag is closed, you can't output logs anywhere.
 * Instead, you can output logs anywhere.<note>The log system is closed by default.</note>
 */
public class Logger {

    /**
     * The flag that represents the log system is opened or closed, default is closed.
     */
    private static boolean DEBUG = false;

    /**
     * The default log output tag.
     */
    private static final String TAG = Logger.class.getSimpleName().toLowerCase();

    public static void close() {
        DEBUG = false;
    }

    public static void open() {
        DEBUG = true;
    }

    public static boolean isOpened() {
        return DEBUG;
    }

    public static boolean isClosed() {
        return !DEBUG;
    }

    public static void info(String msg) {
        info(TAG, msg);
    }

    public static void info(String tag, String msg) {
        if (DEBUG) {
            Log.i(tag, msg);
        }
    }

    public static void error(String msg) {
        error(TAG, msg);
    }

    public static void error(String tag, String msg) {
        if (DEBUG) {
            Log.e(tag, msg);
        }
    }

    public static void debug(String msg) {
        debug(TAG, msg);
    }

    public static void debug(String tag, String msg) {
        if (DEBUG) {
            Log.d(tag, msg);
        }
    }

    public static void warn(String msg) {
        warn(TAG, msg);
    }

    public static void warn(String tag, String msg) {
        if (DEBUG) {
            Log.w(tag, msg);
        }
    }

    public static void verbose(String msg) {
        verbose(TAG, msg);
    }

    public static void verbose(String tag, String msg) {
        if (DEBUG) {
            Log.v(tag, msg);
        }
    }
}
