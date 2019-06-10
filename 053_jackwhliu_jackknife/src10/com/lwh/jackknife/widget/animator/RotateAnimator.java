/*
 *
 *  * Copyright (C) 2017 The JackKnife Open Source Project
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.lwh.jackknife.widget.animator;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.view.View;

public class RotateAnimator extends ActionWrapper<RotateAction> {

    private View mTargetView;
    private static final String ROTATE = "rotate";

    public RotateAnimator() {
        this(null);
    }

    public RotateAnimator(ActionWrapper base) {
        super(base);
    }

    public void setRotate(RotateAction action) {
        float rotate = action.getRotate();
        mTargetView.setRotation(rotate);
    }

    @Override
    public RotateAnimator add(RotateAction action) {
        mActionTree.add(action);
        return this;
    }

    @Override
    public void startAnimation(View view, int duration) {
        super.startAnimation(view, duration);
        this.mTargetView = view;
        this.mActionTree.add(0, new RotateAction(0.0f));
        ObjectAnimator animator = ObjectAnimator.ofObject(this, ROTATE, new RotateEvaluator(),
                mActionTree.toArray());
        animator.setDuration(duration);
        animator.start();
    }

    private static class RotateEvaluator implements TypeEvaluator<RotateAction> {

        @Override
        public RotateAction evaluate(float fraction, RotateAction startValue, RotateAction endValue) {
            RotateAction action;
            float startRotate = startValue.getRotate();
            float endRotate = endValue.getRotate();
            if (endRotate > startRotate) {
                action = new RotateAction(startRotate + fraction * (endRotate - startRotate));
            } else {
                action = new RotateAction(startRotate - fraction * (startRotate - endRotate));
            }
            return action;
        }
    }
}
