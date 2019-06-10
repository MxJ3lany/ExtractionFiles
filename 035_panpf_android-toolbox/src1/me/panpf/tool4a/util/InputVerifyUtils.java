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

import android.content.Context;
import android.view.View;
import android.widget.EditText;

import me.panpf.tool4a.view.animation.ViewAnimationUtils;
import me.panpf.tool4a.widget.ToastUtils;

/**
 * 输入验证工具箱
 */
public class InputVerifyUtils {
    /**
     * 验证给定的值是否合法
     *
     * @param context   上下文
     * @param view      如果验证失败，将会晃动此视图一提醒用户
     * @param vlaue     验证的值
     * @param errorHint 不合法时提示的内容
     * @return 是否合法
     */
    public static boolean valiNullAndEmpty(Context context, View view, String vlaue, String errorHint) {
        boolean result = true;
        if (vlaue == null || "".equals(vlaue)) {
            if (errorHint != null && !"".equals(errorHint)) {
                ToastUtils.toastS(context, errorHint);
            }
            ViewAnimationUtils.shake(view);
            result = false;
        }
        return result;
    }

    /**
     * 检查普通的文本
     *
     * @param editText          待检查的编辑器
     * @param minLength         最小长度，-1忽略此项
     * @param maxLength         最大长度，-1忽略此项
     * @param checkTextListener 检查结果监听器
     * @return
     */
    public static boolean checkText(EditText editText, int minLength, int maxLength, CheckTextListener checkTextListener) {
        boolean result = false;

        String content = editText.getEditableText().toString().trim();

        //检验非空
        if (!"".equals(content)) {
            result = true;
        } else {
            result = false;
            //如果有监听器
            if (checkTextListener != null) {
                checkTextListener.onEmpty(editText, content);
            }
            return result;
        }

        //如果需要检查最小长度
        if (minLength > -1) {
            if (content.length() >= minLength) {
                result = true;
            } else {
                result = false;
                //如果有监听器
                if (checkTextListener != null) {
                    checkTextListener.onMinLengthCheckFailed(editText, content);
                }
                return result;
            }
        }

        //如果需要检查最大长度
        if (maxLength > -1) {
            if (content.length() <= maxLength) {
                result = true;
            } else {
                result = false;
                //如果有监听器
                if (checkTextListener != null) {
                    checkTextListener.onMaxLengthCheckFailed(editText, content);
                }
                return result;
            }
        }

        return result;
    }

    /**
     * 检查结果监听器
     */
    public interface CheckTextListener {
        /**
         * 当是空的
         *
         * @param editText
         * @param content
         */
        public void onEmpty(EditText editText, String content);

        /**
         * 当最小长度检查不通过
         *
         * @param editText
         * @param content
         */
        public void onMinLengthCheckFailed(EditText editText, String content);

        /**
         * 当失败了
         *
         * @param editText
         * @param content
         */
        public void onMaxLengthCheckFailed(EditText editText, String content);
    }
}
