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

package me.panpf.tool4a.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import java.util.Locale;

public class ProgressDialogFragment extends DialogFragment {
    public static String MESSAGE_DEFAULT_CHINA = "请稍等片刻...";
    public static String MESSAGE_DEFAULT_OTHER = "Please wait a moment...";
    public static String FRAGMENT_TAG_PROGRESS_DIALOG = "FRAGMENT_TAG_PROGRESS_DIALOG";

    private Builder builder;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        applyParams(progressDialog);
        return progressDialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (builder.onDismissListener != null) {
            builder.onDismissListener.onDismiss(dialog);
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (builder.onCancelListener != null) {
            builder.onCancelListener.onCancel(dialog);
        }
    }

    /**
     * 设置Builder
     *
     * @param builder Builder
     */
    private void setBuilder(Builder builder) {
        this.builder = builder;
    }

    /**
     * 应用参数
     *
     * @param progressDialog 对话框
     */
    private void applyParams(ProgressDialog progressDialog) {
        if (builder == null)
            throw new IllegalArgumentException("builder 为null 你需要调用setBuilder()方法设置Builder");

        progressDialog.setTitle(builder.title);
        progressDialog.setMessage(builder.message != null ? builder.message : (Locale.CHINA.equals(Locale.getDefault()) ? MESSAGE_DEFAULT_CHINA : MESSAGE_DEFAULT_OTHER));
        if (builder.confirmButtonName != null) {
            progressDialog.setButton(AlertDialog.BUTTON_POSITIVE, builder.confirmButtonName, builder.confirmButtonClickListener);
        }
        if (builder.cancelButtonName != null) {
            progressDialog.setButton(AlertDialog.BUTTON_NEGATIVE, builder.cancelButtonName, builder.cancelButtonClickListener);
        }
        if (builder.neutralButtonName != null) {
            progressDialog.setButton(AlertDialog.BUTTON_NEUTRAL, builder.neutralButtonName, builder.neutralButtonClickListener);
        }
        progressDialog.setOnKeyListener(builder.onKeyListener);
        progressDialog.setOnShowListener(builder.onShowListener);
        setCancelable(builder.cancelable);
    }

    /**
     * 更新
     */
    public void update() {
        Dialog dialog = getDialog();
        if (dialog == null || !(dialog instanceof ProgressDialog)) return;
        applyParams((ProgressDialog) dialog);
    }

    /**
     * 显示一个进度对话框
     *
     * @param fragmentManager Fragment管理器
     * @param builder         构建器
     */
    public static void show(FragmentManager fragmentManager, Builder builder) {
        if (fragmentManager == null) return;

        Fragment fragment = fragmentManager.findFragmentByTag(FRAGMENT_TAG_PROGRESS_DIALOG);
        boolean old = fragment != null && fragment instanceof ProgressDialogFragment;
        ProgressDialogFragment progressDialogFragment = old ? ((ProgressDialogFragment) fragment) : new ProgressDialogFragment();
        progressDialogFragment.setBuilder(builder);
        if (old) {
            progressDialogFragment.update();
        } else {
            progressDialogFragment.show(fragmentManager, FRAGMENT_TAG_PROGRESS_DIALOG);
        }
    }

    /**
     * 关闭进度对话框
     *
     * @param fragmentManager Fragment管理器
     */
    public static void close(FragmentManager fragmentManager) {
        if (fragmentManager == null) return;

        Fragment fragment = fragmentManager.findFragmentByTag(FRAGMENT_TAG_PROGRESS_DIALOG);
        if (fragment != null && fragment instanceof ProgressDialogFragment) {
            ((ProgressDialogFragment) fragment).dismiss();
        }
    }

    public static class Builder {
        private String title;
        private String message;
        private String confirmButtonName;
        private String cancelButtonName;
        private String neutralButtonName;
        private boolean cancelable = false;
        private DialogInterface.OnClickListener confirmButtonClickListener;
        private DialogInterface.OnClickListener cancelButtonClickListener;
        private DialogInterface.OnClickListener neutralButtonClickListener;
        private DialogInterface.OnShowListener onShowListener;
        private DialogInterface.OnDismissListener onDismissListener;
        private DialogInterface.OnCancelListener onCancelListener;
        private DialogInterface.OnKeyListener onKeyListener;

        public Builder(String message) {
            this.message = message;
        }

        /**
         * 设置标题
         *
         * @param title 标题
         * @return Builder
         */
        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        /**
         * 设置消息
         *
         * @param message 消息
         * @return Builder
         */
        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        /**
         * 设置是否可以取消
         *
         * @param cancelable 是否可以取消
         * @return Builder
         */
        public Builder setCancelable(boolean cancelable) {
            this.cancelable = cancelable;
            return this;
        }

        /**
         * 设置确定按钮
         *
         * @param name 按钮名称
         * @return Builder
         */
        public Builder setConfirmButton(String name) {
            this.confirmButtonName = name;
            return this;
        }

        /**
         * 设置确定按钮
         *
         * @param name          按钮名称
         * @param clickListener 点击监听器
         * @return Builder
         */
        public Builder setConfirmButton(String name, DialogInterface.OnClickListener clickListener) {
            this.confirmButtonName = name;
            this.confirmButtonClickListener = clickListener;
            return this;
        }

        /**
         * 设置取消按钮
         *
         * @param name 按钮名称
         * @return Builder
         */
        public Builder setCancelButton(String name) {
            this.cancelButtonName = name;
            return this;
        }

        /**
         * 设置取消按钮
         *
         * @param name          按钮名称
         * @param clickListener 点击监听器
         * @return Builder
         */
        public Builder setCancelButton(String name, DialogInterface.OnClickListener clickListener) {
            this.cancelButtonName = name;
            this.cancelButtonClickListener = clickListener;
            return this;
        }

        /**
         * 设置中立按钮
         *
         * @param name 按钮名称
         * @return Builder
         */
        public Builder setNeutralButton(String name) {
            this.neutralButtonName = name;
            return this;
        }

        /**
         * 设置中立按钮
         *
         * @param name          按钮名称
         * @param clickListener 点击监听器
         * @return Builder
         */
        public Builder setNeutralButton(String name, DialogInterface.OnClickListener clickListener) {
            this.neutralButtonName = name;
            this.neutralButtonClickListener = clickListener;
            return this;
        }

        /**
         * 设置显示监听器
         *
         * @param onShowListener 显示监听器
         */
        public Builder setOnShowListener(DialogInterface.OnShowListener onShowListener) {
            this.onShowListener = onShowListener;
            return this;
        }

        /**
         * 设置解除监听器
         *
         * @param onDismissListener 解除监听器
         */
        public Builder setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
            this.onDismissListener = onDismissListener;
            return this;
        }

        /**
         * 设置取消监听器
         *
         * @param onCancelListener 取消监听器
         */
        public Builder setOnCancelListener(DialogInterface.OnCancelListener onCancelListener) {
            this.onCancelListener = onCancelListener;
            return this;
        }

        /**
         * 设置按键监听器
         *
         * @param onKeyListener 按键监听器
         */
        public Builder setOnKeyListener(DialogInterface.OnKeyListener onKeyListener) {
            this.onKeyListener = onKeyListener;
            return this;
        }

        /**
         * 显示
         *
         * @param fragmentManager Fragment管理器
         */
        public void show(FragmentManager fragmentManager) {
            ProgressDialogFragment.show(fragmentManager, this);
        }
    }
}