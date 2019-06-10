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

import java.lang.ref.WeakReference;

public abstract class BasePresenter<V extends IBaseView> {

    protected WeakReference<V> mViewRef;

    public void attachView(V view) {
        mViewRef = new WeakReference<>(view);
    }

    public void detachView() {
        if (mViewRef != null) {
            mViewRef.clear();
            mViewRef = null;
        }
    }

    protected V getView() {
        return mViewRef.get();
    }

    /**
     * It's usually called before {@link #getView()}.
     *
     * @return Ture means successful attachment, false otherwise.
     */
    protected boolean isViewAttached() {
        return mViewRef != null && mViewRef.get() != null;
    }
}
