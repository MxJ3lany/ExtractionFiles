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

package com.lwh.jackknife.widget.popupdialog;

import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.lwh.jackknife.widget.R;

/**
 * Add to decor view.
 */
public abstract class AbstractDialogView {

    protected static final int INVALID = -1;
    protected static final int INVALID_COLOR = 0;
    public static final int DEFAULT_SHADOW_COLOR = 0x60000000;
    protected OnCancelListener mOnCancelListener;
    protected View.OnKeyListener mOnBackListener;
    protected FrameLayout.LayoutParams mGravityLayoutParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM
    );
    protected int mGravity = Gravity.NO_GRAVITY;
    protected final FrameLayout.LayoutParams mShadowLayoutParams = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
    );
    protected boolean mNeedShadowView = false;
    protected int mShadowColor = INVALID_COLOR;

    public interface OnCancelListener {
        void onCancel();
    }

    public void setGravityLayoutParams(FrameLayout.LayoutParams flp) {
        this.mGravityLayoutParams = flp;
    }

    public FrameLayout.LayoutParams getGravityLayoutParams() {
        return mGravityLayoutParams;
    }

    public FrameLayout.LayoutParams getShadowLayoutParams() {
        return mShadowLayoutParams;
    }

    public void setGravity(int gravity) {
        this.mGravity = gravity;
    }

    public int getGravity() {
        return mGravity;
    }

    /**
     * Add content view to decor view.
     *
     * @param inflater
     * @param parent   decor view
     * @return content view
     */
    protected View performInflateView(LayoutInflater inflater, FrameLayout parent) {
        View dialogView = inflater.inflate(R.layout.jknf_dialog_view, parent, false);
        LinearLayout dialogViewRoot = (LinearLayout) dialogView.findViewById(R.id
                .jknf_dialog_view_content);
        if (mGravity != Gravity.NO_GRAVITY) {
            dialogViewRoot.setGravity(mGravity);
        }
        dialogViewRoot.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (mOnBackListener != null) {
                    mOnBackListener.onKey(v, keyCode, event);
                }
                return false;
            }
        });
        addContent(inflater, parent, dialogViewRoot);
        return dialogView;
    }

    protected abstract View getContentView();

    protected abstract void addContent(LayoutInflater inflater, ViewGroup parent, LinearLayout viewRoot);

    protected abstract void initShadow(int shadowColor);

    protected abstract void setShadowViewOutsideCanDismiss(View shadeView, boolean canDismiss);

    public void setOnCancelListener(OnCancelListener listener) {
        this.mOnCancelListener = listener;
    }

    protected void setOnBackListener(View.OnKeyListener listener) {
        this.mOnBackListener = listener;
    }
}
