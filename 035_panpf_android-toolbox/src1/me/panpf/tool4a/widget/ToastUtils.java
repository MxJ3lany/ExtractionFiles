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

package me.panpf.tool4a.widget;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

/**
 * Toast工具箱
 */
public class ToastUtils {
    /**
     * 吐出指定的视图，使其显示较长的时间
     *
     * @param context
     * @param view
     */
    public static void toastL(Context context, View view) {
        Toast toast = new Toast(context);
        toast.setView(view);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.show();
    }

    /**
     * 吐出指定的视图，使其显示较短的时间
     *
     * @param context
     * @param view
     */
    public static void toastS(Context context, View view) {
        Toast toast = new Toast(context);
        toast.setView(view);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * 吐出一个显示时间较长的提示
     *
     * @param context 上下文对象
     * @param resId   显示内容资源ID
     */
    public static void toastL(Context context, int resId) {
        Toast.makeText(context, resId, Toast.LENGTH_LONG).show();
    }

    /**
     * 吐出一个显示时间较短的提示
     *
     * @param context 上下文对象
     * @param resId   显示内容资源ID
     */
    public static void toastS(Context context, int resId) {
        Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();
    }

    /**
     * 吐出一个显示时间较长的提示
     *
     * @param context 上下文对象
     * @param content 显示内容
     */
    public static void toastL(Context context, String content) {
        Toast.makeText(context, content, Toast.LENGTH_LONG).show();
    }

    /**
     * 吐出一个显示时间较短的提示
     *
     * @param context 上下文对象
     * @param content 显示内容
     */
    public static void toastS(Context context, String content) {
        Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
    }

    /**
     * 吐出一个显示时间较长的提示
     *
     * @param context     上下文对象
     * @param formatResId 被格式化的字符串资源的ID
     * @param args        参数数组
     */
    public static void toastL(Context context, int formatResId, Object... args) {
        Toast.makeText(context, String.format(context.getString(formatResId), args), Toast.LENGTH_LONG).show();
    }

    /**
     * 吐出一个显示时间较短的提示
     *
     * @param context     上下文对象
     * @param formatResId 被格式化的字符串资源的ID
     * @param args        参数数组
     */
    public static void toastS(Context context, int formatResId, Object... args) {
        Toast.makeText(context, String.format(context.getString(formatResId), args), Toast.LENGTH_SHORT).show();
    }

    /**
     * 吐出一个显示时间较长的提示
     *
     * @param context 上下文对象
     * @param format  被格式化的字符串
     * @param args    参数数组
     */
    public static void toastL(Context context, String format, Object... args) {
        Toast.makeText(context, String.format(format, args), Toast.LENGTH_LONG).show();
    }

    /**
     * 吐出一个显示时间较短的提示
     *
     * @param context 上下文对象
     * @param format  被格式化的字符串
     * @param args    参数数组
     */
    public static void toastS(Context context, String format, Object... args) {
        Toast.makeText(context, String.format(format, args), Toast.LENGTH_SHORT).show();
    }

}