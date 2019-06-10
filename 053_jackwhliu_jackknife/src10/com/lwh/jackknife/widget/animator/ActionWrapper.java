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

package com.lwh.jackknife.widget.animator;

import android.view.View;

import java.util.ArrayList;
import java.util.List;

public abstract class ActionWrapper<A extends Action> implements Action<A> {

    protected View mTargetView;
    protected ActionWrapper mBase;
    protected List<A> mActionTree;

    protected ActionWrapper() {
        this.mActionTree = new ArrayList<>();
    }

    protected ActionWrapper(ActionWrapper base) {
        this();
        this.mBase = base;
    }

    @Override
    public Action add(A action) {
        if (mBase != null) {
            return mBase.add(action);
        }
        return this;
    }

    @Override
    public void startAnimation(View view, int duration) {
        if (mBase != null) {
            mBase.startAnimation(view, duration);
        }
    }
}
