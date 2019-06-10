/*
 * Copyright (C) 2019 Peter Gregus for GravityBox Project (C3C076@xda)
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
package com.ceco.pie.gravitybox;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;

import de.robv.android.xposed.XposedBridge;

public class ColorUtils {
    private static final String TAG = "GB:" + ColorUtils.class.getSimpleName();
    private static final boolean DEBUG = false;

    private static void log(String message) {
        XposedBridge.log(TAG + ": " + message);
    }

    public static int alphaPercentToInt(int percentAlpha) {
        percentAlpha = Math.min(Math.max(percentAlpha, 0), 100);
        float alpha = (float)percentAlpha / 100f;
        return (alpha == 0 ? 255 : (int)(1-alpha * 255));
    }

    public static int getColorFromStyleAttr(Context ctx, int attrId) {
        if (attrId == 0)
            return 0;

        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = ctx.getTheme();
        theme.resolveAttribute(attrId, typedValue, true);
        TypedArray arr = ctx.obtainStyledAttributes(
                typedValue.data, new int[] { attrId });
        int color = arr.getColor(0, -1);
        arr.recycle();
        return color;
    }

    public static int getThemeAttr(Context ctx, int attrId) {
        if (attrId == 0)
            return 0;

        TypedArray ta = ctx.obtainStyledAttributes(new int[]{attrId});
        int theme = ta.getResourceId(0, 0);
        ta.recycle();
        return theme;
    }

    public static int getSystemUiSingleToneColor(Context ctx, String iconThemeName, int defaultColor) {
        try {
            int color = defaultColor;
            final int themeId = getThemeAttr(ctx, ctx.getResources().getIdentifier(
                    iconThemeName, "attr", ctx.getPackageName()));
            if (DEBUG) log(iconThemeName + " ID=" + themeId);
            final int colorResId = ctx.getResources().getIdentifier(
                    "singleToneColor", "attr", ctx.getPackageName());
            if (DEBUG) log("singleToneColor Res ID=" + colorResId);
            if (themeId != 0 && colorResId != 0) {
                Context themedCtx = new ContextThemeWrapper(ctx, themeId);
                color = getColorFromStyleAttr(themedCtx, colorResId);
            }
            if (DEBUG) log("getSystemUiSingleToneColor: color=" +
                    Integer.toHexString(color));
            return color;
        } catch (Throwable t) {
            GravityBox.log(TAG, "getSystemUiSingleToneColor:", t);
            return defaultColor;
        }
    }

    public static int compositeColors(int foreground, int background) {
        int bgAlpha = Color.alpha(background);
        int fgAlpha = Color.alpha(foreground);
        int a = compositeAlpha(fgAlpha, bgAlpha);
        int r = compositeComponent(Color.red(foreground), fgAlpha,
                Color.red(background), bgAlpha, a);
        int g = compositeComponent(Color.green(foreground), fgAlpha,
                Color.green(background), bgAlpha, a);
        int b = compositeComponent(Color.blue(foreground), fgAlpha,
                Color.blue(background), bgAlpha, a);
        return Color.argb(a, r, g, b);
    }

    private static int compositeAlpha(int foregroundAlpha, int backgroundAlpha) {
        return 0xFF - (((0xFF - backgroundAlpha) * (0xFF - foregroundAlpha)) / 0xFF);
    }

    private static int compositeComponent(int fgC, int fgA, int bgC, int bgA, int a) {
        if (a == 0) return 0;
        return ((0xFF * fgC * fgA) + (bgC * bgA * (0xFF - fgA))) / (a * 0xFF);
    }

    public static int blendAlpha(int color, float alpha) {
        final float newAlpha = alpha < 0f ? 0f : (alpha > 1f ? 1f : alpha);
        final float colorAlpha = Color.alpha(color) / 255f;
        final int alphaInt = (int) (255 * newAlpha * colorAlpha); // Blend by multiplying
        // Ensure alpha is clamped [0-255] or ColorUtils will crash
        return setAlphaComponent(color, alphaInt);
    }

    public static int setAlphaComponent(int color, int alpha) {
        if (alpha < 0 || alpha > 255) {
            throw new IllegalArgumentException("alpha must be between 0 and 255.");
        }
        return (color & 0x00ffffff) | (alpha << 24);
    }
}
