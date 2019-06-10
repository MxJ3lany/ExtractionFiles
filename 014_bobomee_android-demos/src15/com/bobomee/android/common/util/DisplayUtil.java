/*
 * Copyright (C) 2016.  BoBoMEe(wbwjx115@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.bobomee.android.common.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

/**
 * 屏幕工具
 * <link>
 *   https://github.com/liaohuqiu/cube-sdk/blob/master/core/src/in/srain/cube/util/LocalDisplay.java
 * </link>
 */
public class DisplayUtil {

  private DisplayUtil() {
  }

  public static int SCREEN_WIDTH_PIXELS;
  public static int SCREEN_HEIGHT_PIXELS;
  public static float SCREEN_DENSITY;
  public static int SCREEN_WIDTH_DP;
  public static int SCREEN_HEIGHT_DP;
  private static boolean sInitialed;

  public static void init(Context context) {
    if (sInitialed || context == null) {
      return;
    }
    sInitialed = true;
    DisplayMetrics dm = new DisplayMetrics();
    WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    wm.getDefaultDisplay().getMetrics(dm);
    SCREEN_WIDTH_PIXELS = dm.widthPixels;
    SCREEN_HEIGHT_PIXELS = dm.heightPixels;
    SCREEN_DENSITY = dm.density;
    SCREEN_WIDTH_DP = (int) (SCREEN_WIDTH_PIXELS / dm.density);
    SCREEN_HEIGHT_DP = (int) (SCREEN_HEIGHT_PIXELS / dm.density);
  }

  public static int dp2px(float dp) {
    final float scale = SCREEN_DENSITY;
    return (int) (dp * scale + 0.5f);
  }

  public static int px2dp(float pxValue) {
    final float scale = SCREEN_DENSITY;
    return (int) (pxValue / scale + 0.5f);
  }

  public static int sp2px(float spValue) {
    float fontScale = SCREEN_DENSITY;
    return (int) (spValue * fontScale + 0.5f);
  }

  public static int designedDP2px(float designedDp) {
    if (SCREEN_WIDTH_DP != 320) {
      designedDp = designedDp * SCREEN_WIDTH_DP / 320f;
    }
    return dp2px(designedDp);
  }

  public static void setPadding(final View view, float left, float top, float right, float bottom) {
    view.setPadding(designedDP2px(left), dp2px(top), designedDP2px(right), dp2px(bottom));
  }
}