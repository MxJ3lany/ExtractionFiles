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

import java.util.List;

public class AnimatorBuilder {

    ActionWrapper mBase;
    AlphaAnimator mAlphaAnimator;
    RotateAnimator mRotateAnimator;
    ScaleAnimator mScaleAnimator;
    PathAnimator mPathAnimator;

    public AnimatorBuilder alpha(List<AlphaAction> actions) {
        if (mBase != null) {
            mAlphaAnimator = new AlphaAnimator(mBase);
        } else {
            mAlphaAnimator = new AlphaAnimator();
        }
        for (AlphaAction action : actions) {
            mAlphaAnimator.add(action);
        }
        mBase = mAlphaAnimator;
        return this;
    }

    public AnimatorBuilder rotate(List<RotateAction> actions) {
        if (mBase != null) {
            mRotateAnimator = new RotateAnimator(mBase);
        } else {
            mRotateAnimator = new RotateAnimator();
        }
        for (RotateAction action : actions) {
            mRotateAnimator.add(action);
        }
        mBase = mRotateAnimator;
        return this;
    }

    public AnimatorBuilder scale(List<ScaleAction> actions) {
        if (mBase != null) {
            mScaleAnimator = new ScaleAnimator(mBase);
        } else {
            mScaleAnimator = new ScaleAnimator();
        }
        for (ScaleAction action : actions) {
            mScaleAnimator.add(action);
        }
        mBase = mScaleAnimator;
        return this;
    }

    public <PA extends PathAction> AnimatorBuilder path(List<PA> actions) {
        if (mBase != null) {
            this.mPathAnimator = new PathAnimator(mBase);
        } else {
            this.mPathAnimator = new PathAnimator();
        }
        for (PathAction action : actions) {
            this.mPathAnimator.add(action);
        }
        mBase = mPathAnimator;
        return this;
    }

    public ActionWrapper build() {
        if (mBase == null) {
            throw new RuntimeException("You haven\'t assigned any animators.");
        }
        return mBase;
    }
}
