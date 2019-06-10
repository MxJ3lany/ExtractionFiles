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

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class DialogView extends AbstractDialogView {

    private int mViewResId = View.NO_ID;
    protected View mContentView;
    private boolean mCanTouchOutside;
    private OnInflateListener mOnInflateListener;

    protected DialogView() {
    }

    public DialogView(int layoutResId) {
        this.mViewResId = layoutResId;
    }

    public DialogView(View view) {
        this.mContentView = view;
    }

    public DialogView(int layoutResId, int shadowColor) {
        this(layoutResId);
        initShadow(shadowColor);
    }

    public DialogView(View view, int shadowColor) {
        this(view);
        initShadow(shadowColor);
    }

    public interface OnInflateListener {
        void onInflateFinish(View contentView);
    }

    public void setOnInflateListener(OnInflateListener listener) {
        this.mOnInflateListener = listener;
    }

    /**
     * Only valid if the shadow background is set.
     */
    public void setCanTouchOutside(boolean canTouchOutside) {
        this.mCanTouchOutside = canTouchOutside;
    }

    public boolean isNeedShadowView() {
        return mNeedShadowView;
    }

    public int getShadowColor() {
        return mShadowColor;
    }

    public void setContentView(View contentView) {
        this.mContentView = contentView;
    }

    /**
     * Must invoke after {@link OnInflateListener#onInflateFinish(View)}.
     */
    public View findViewById(int resId) {
        return mContentView.findViewById(resId);
    }

    @Override
    protected View getContentView() {
        return mContentView;
    }

    @Override
    protected void addContent(LayoutInflater inflater, ViewGroup parent, LinearLayout viewRoot) {
        if (mNeedShadowView) {
            viewRoot.setBackgroundColor(mShadowColor);
            setShadowViewOutsideCanDismiss(viewRoot, true);
        }
        if (mViewResId != View.NO_ID) {
            mContentView = inflater.inflate(mViewResId, viewRoot, false); //inflate layout
        }
        if (mOnInflateListener != null) {
            mOnInflateListener.onInflateFinish(mContentView);
        }
        mContentView.setFocusable(true);
        mContentView.setFocusableInTouchMode(true);
        mContentView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (mOnBackListener != null) {
                    mOnBackListener.onKey(v, keyCode, event);
                }
                return true;
            }
        });
        mContentView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        viewRoot.addView(mContentView);
    }

    @Override
    protected void setShadowViewOutsideCanDismiss(View shadeView, final boolean canDismiss) {
        shadeView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (canDismiss) {
                    mOnCancelListener.onCancel();
                }
                return !mCanTouchOutside;
            }
        });
    }

    @Override
    protected void initShadow(int shadowColor) {
        this.mNeedShadowView = true;
        this.mShadowColor = shadowColor;
    }
}
