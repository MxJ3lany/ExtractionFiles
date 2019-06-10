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

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.lang.ref.WeakReference;

public class ToastUtils {

    private static WeakReference<Toast> sToastRef;
    private static Handler sHandler = new Handler(Looper.getMainLooper());

    private ToastUtils() {
    }

    public static void showShort(Context context, String text) {
        showToast(context, text, Toast.LENGTH_SHORT);
    }

    public static void showLong(Context context, String text) {
        showToast(context, text, Toast.LENGTH_LONG);
    }

    public static void showShort(Context context, int resId) {
        showToast(context, resId, Toast.LENGTH_SHORT);
    }

    public static void showLong(Context context, int resId) {
        showToast(context, resId, Toast.LENGTH_LONG);
    }

    private static void showToast(final Context context, final int resId, final int duration) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            sHandler.post(new Runnable() {
                @Override
                public void run() {
                    showToastInternal(context, resId, duration);
                }
            });
        } else {
            showToastInternal(context, resId, duration);
        }
    }

    private static void showToast(final Context context, final String text, final int duration) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            sHandler.post(new Runnable() {
                @Override
                public void run() {
                    showToastInternal(context, text, duration);
                }
            });
        } else {
            showToastInternal(context, text, duration);
        }
    }

    private static void showToastInternal(Context context, String text, int duration) {
        Toast toast;
        if (sToastRef == null || sToastRef.get() == null) {
            toast = Toast.makeText(context, text, duration);
            sToastRef = new WeakReference<>(toast);
        } else {
            toast = sToastRef.get();
            if (toast != null) {
                toast.setDuration(duration);
                toast.setText(text);
            } else return;
        }
        toast.show();
    }

    private static void showToastInternal(Context context, int resId, int duration) {
        Toast toast;
        if (sToastRef == null || sToastRef.get() == null) {
            toast = Toast.makeText(context, resId, duration);
            sToastRef = new WeakReference<>(toast);
        } else {
            toast = sToastRef.get();
            if (toast != null) {
                toast.setDuration(duration);
                toast.setText(resId);
            } else return;
        }
        toast.show();
    }
}
