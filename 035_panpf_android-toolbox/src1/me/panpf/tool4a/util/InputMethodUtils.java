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

package me.panpf.tool4a.util;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Selection;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class InputMethodUtils {

    /**
     * 移动光标到已输入文本的最后
     */
    @SuppressWarnings("WeakerAccess")
    public static void moveCursorToEnd(EditText editText) {
        Editable editable = editText.getEditableText();
        Selection.setSelection(editable, editable.toString().length());
    }

    /**
     * 显示输入法并将焦点定位到指定的输入框，并可指定是否移动光标到已输入文本的最后
     *
     * @param editText        输入框
     * @param moveCursorToEnd 是否移动光标到已输入文本的最后
     */
    @SuppressWarnings("WeakerAccess")
    public static void showSoftInput(EditText editText, boolean moveCursorToEnd) {
        // 这一步是必须的，且必须在前
        editText.requestFocus();

        InputMethodManager imm = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);

        // 定位光标到已输入文本的最后
        if (moveCursorToEnd) {
            moveCursorToEnd(editText);
        }
    }

    /**
     * 显示输入法并将焦点定位到指定的输入框，最后移动光标到已输入文本的最后
     *
     * @param editText 输入框
     */
    public static void showSoftInput(EditText editText) {
        showSoftInput(editText, true);
    }

    /**
     * 延迟100毫秒显示输入法并将焦点定位到指定的输入框，最后移动光标到已输入文本的最后
     */
    public static void delayShowSoftInput(final EditText editText) {
        // 定位光标到已输入文本的最后，定位光标不能延迟，要不然页面上看上去会有光标的跳动
        moveCursorToEnd(editText);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodUtils.showSoftInput(editText, false);
            }
        }, 100);
    }

    /**
     * 隐藏软键盘
     */
    public static void hideSoftInput(EditText editText) {
        if (editText == null || editText.getWindowToken() == null) {
            return;
        }

        InputMethodManager imm = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    /**
     * 隐藏软键盘
     */
    public static void hideSoftInput(Activity activity) {
        View currentFocusView = activity != null ? activity.getCurrentFocus() : null;
        if (currentFocusView == null) {
            return;
        }

        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(currentFocusView.getWindowToken(), 0);
    }

    /**
     * 隐藏软键盘
     */
    @SuppressWarnings("unused")
    public static void hideSoftInput(Fragment fragment) {
        hideSoftInput(fragment != null ? fragment.getActivity() : null);
    }
}
