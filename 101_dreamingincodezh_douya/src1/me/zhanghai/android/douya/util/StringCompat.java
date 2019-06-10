/*
 * Copyright (c) 2017 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.douya.util;

import android.os.Build;
import androidx.annotation.NonNull;

public class StringCompat {

    private StringCompat() {}

    @NonNull
    public static String join(@NonNull CharSequence delimiter, @NonNull CharSequence... elements) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return String.join(delimiter, elements);
        } else {
            // TextUtils.join() can throw NullPointerException, so we cannot use it.
            StringBuilder builder = new StringBuilder();
            boolean first = true;
            for (CharSequence element : elements) {
                if (first) {
                    first = false;
                } else {
                    builder.append(delimiter);
                }
                builder.append(element);
            }
            return builder.toString();
        }
    }

    @NonNull
    public static String join(@NonNull CharSequence delimiter,
                              @NonNull Iterable<? extends CharSequence> elements) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return String.join(delimiter, elements);
        } else {
            // TextUtils.join() can throw NullPointerException, so we cannot use it.
            StringBuilder builder = new StringBuilder();
            boolean first = true;
            for (CharSequence element : elements) {
                if (first) {
                    first = false;
                } else {
                    builder.append(delimiter);
                }
                builder.append(element);
            }
            return builder.toString();
        }
    }
}
