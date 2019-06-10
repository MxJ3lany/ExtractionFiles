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
import android.util.TypedValue;

public class DensityUtils {

    private DensityUtils() {
    }

    public static float dp2px(float dpVal, Context context) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics());
    }

    public static float sp2px(float spVal, Context context) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spVal, context.getResources().getDisplayMetrics());
    }

    public static float px2dp(float pxVal, Context context) {
        float scale = context.getResources().getDisplayMetrics().density;
        return pxVal / scale;
    }

    public static float px2sp(float pxVal, Context context) {
        float scale = context.getResources().getDisplayMetrics().scaledDensity;
        return pxVal / scale;
    }
}
