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

package com.lwh.jackknife.mvp;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

public abstract class BaseDialog<V extends IBaseView, P extends BasePresenter<V>> extends Dialog {

    protected P mPresenter;

    protected final String TAG = getClass().getSimpleName();

    protected abstract P createPresenter();

    public BaseDialog(Context context) {
        super(context);
    }

    public BaseDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    protected BaseDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        mPresenter = createPresenter();
        mPresenter.attachView((V) this);
    }

    @Override
    public void onAttachedToWindow() {
        Log.i(TAG, "onAttachedToWindow()");
        super.onAttachedToWindow();
    }

    @Override
    public void onDetachedFromWindow() {
        Log.i(TAG, "onDetachedFromWindow()");
        super.onDetachedFromWindow();
        mPresenter.detachView();
    }
}
