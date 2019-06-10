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

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.view.View;

public class ScaleAnimator extends ActionWrapper<ScaleAction> {

    private final String SCALE = "scale";

    public ScaleAnimator() {
        this(null);
    }

    public ScaleAnimator(ActionWrapper base) {
        super(base);
    }

    @Override
    public ScaleAnimator add(ScaleAction action) {
        mActionTree.add(action);
        return this;
    }

    @Override
    public void startAnimation(View view, int duration) {
        super.startAnimation(view, duration);
        mTargetView = view;
        mActionTree.add(0, new ScaleAction(1.0f, 1.0f));
        ObjectAnimator animator = ObjectAnimator.ofObject(this, SCALE, new ScaleEvaluator(),
                mActionTree.toArray());
        animator.setDuration(duration);
        animator.start();
    }

    public void setScale(ScaleAction action) {
        float scaleX = action.getScaleX();
        float scaleY = action.getScaleY();
        mTargetView.setScaleX(scaleX);
        mTargetView.setScaleY(scaleY);
    }

    private class ScaleEvaluator implements TypeEvaluator<ScaleAction> {

        @Override
        public ScaleAction evaluate(float fraction, ScaleAction startValue, ScaleAction endValue) {
            float startScaleX = startValue.getScaleX();
            float startScaleY = startValue.getScaleY();
            float endScaleX = endValue.getScaleX();
            float endScaleY = endValue.getScaleY();
            float scaleX;
            float scaleY;
            if (endScaleX > startScaleX) {
                scaleX = startScaleX + fraction * (endScaleX - startScaleX);
            } else {
                scaleX = startScaleX - fraction * (startScaleX - endScaleX);
            }
            if (endScaleY > startScaleY) {
                scaleY = startScaleY + fraction * (endScaleY - startScaleY);
            } else {
                scaleY = startScaleY - fraction * (startScaleY - endScaleY);
            }
            return new ScaleAction(scaleX, scaleY);
        }
    }
}
