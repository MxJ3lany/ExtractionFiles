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

package com.lwh.jackknife.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class ScreenUtils {

    private static ScreenUtils sInstance;
    private static int[] sMetrics;

    private ScreenUtils() {
    }

    public static ScreenUtils getInstance(Context context) {
        if (sInstance == null) {
            synchronized (ScreenUtils.class) {
                if (sInstance == null) {
                    sInstance = new ScreenUtils();
                    sMetrics = _getScreenWH(context);
                }
            }
        }
        return sInstance;
    }

    private static int[] _getScreenWH(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return new int[]{outMetrics.widthPixels, outMetrics.heightPixels};
    }

    public int getScreenWidth() {
        return sMetrics[0];
    }

    public int getScreenHeight() {
        return sMetrics[1];
    }
}
