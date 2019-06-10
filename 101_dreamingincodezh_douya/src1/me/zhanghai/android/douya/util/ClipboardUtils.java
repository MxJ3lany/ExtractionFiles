/*
 * Copyright (c) 2015 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.douya.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;

import me.zhanghai.android.douya.R;
import me.zhanghai.android.douya.network.api.info.ClipboardCopyable;

public class ClipboardUtils {

    private static final int TOAST_COPIED_TEXT_MAX_LENGTH = 40;

    private ClipboardUtils() {}

    @NonNull
    private static ClipboardManager getClipboardManager(@NonNull Context context) {
        return ContextCompat.getSystemService(context, ClipboardManager.class);
    }

    @Nullable
    public static CharSequence readText(@NonNull Context context) {
        ClipData clipData = getClipboardManager(context).getPrimaryClip();
        if (clipData == null || clipData.getItemCount() == 0) {
            return null;
        }
        return clipData.getItemAt(0).coerceToText(context);
    }

    public static void copyText(@Nullable CharSequence label, @NonNull CharSequence text,
                                @NonNull Context context) {
        ClipData clipData = ClipData.newPlainText(label, text);
        getClipboardManager(context).setPrimaryClip(clipData);
        showToast(text, context);
    }

    public static void copyText(@Nullable CharSequence text, @NonNull Context context) {
        copyText(null, text, context);
    }

    public static void copy(ClipboardCopyable copyable, Context context) {
        copyText(copyable.getClipboardLabel(context), copyable.getClipboardText(context), context);
    }

    private static void showToast(@NonNull CharSequence copiedText, @NonNull Context context) {
        boolean ellipsized = false;
        if (copiedText.length() > TOAST_COPIED_TEXT_MAX_LENGTH) {
            copiedText = copiedText.subSequence(0, TOAST_COPIED_TEXT_MAX_LENGTH);
            ellipsized = true;
        }
        int indexOfFirstNewline = TextUtils.indexOf(copiedText, '\n');
        if (indexOfFirstNewline != -1) {
            int indexOfSecondNewline = TextUtils.indexOf(copiedText, '\n', indexOfFirstNewline + 1);
            if (indexOfSecondNewline != -1) {
                copiedText = copiedText.subSequence(0, indexOfSecondNewline);
                ellipsized = true;
            }
        }
        if (ellipsized) {
            copiedText = copiedText.toString() + '\u2026';
        }
        ToastUtils.show(context.getString(R.string.copied_to_clipboard_format, copiedText),
                context);
    }
}
