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

public class AlphaAnimator extends ActionWrapper<AlphaAction> {

    private static final String ALPHA = "alpha";

    public AlphaAnimator() {
        this(null);
    }

    public AlphaAnimator(ActionWrapper base) {
        super(base);
    }

    @Override
    public AlphaAnimator add(AlphaAction action) {
        super.add(action);
        mActionTree.add(action);
        return this;
    }

    @Override
    public void startAnimation(View view, int duration) {
        super.startAnimation(view, duration);
        this.mTargetView = view;
        this.mActionTree.add(0, new AlphaAction(1.0f));
        ObjectAnimator animator = ObjectAnimator.ofObject(this, ALPHA, new AlphaEvaluator(),
                mActionTree.toArray());
        animator.setDuration(duration);
        animator.start();
    }

    public void setAlpha(AlphaAction action) {
        float alpha = action.getAlpha();
        mTargetView.setAlpha(alpha);
    }

    private static class AlphaEvaluator implements TypeEvaluator<AlphaAction> {

        @Override
        public AlphaAction evaluate(float fraction, AlphaAction startValue, AlphaAction endValue) {
            AlphaAction action;
            float startAlpha = startValue.getAlpha();
            float endAlpha = endValue.getAlpha();
            if (endAlpha > startAlpha) {
                action = new AlphaAction(startAlpha + fraction * (endAlpha - startAlpha));
            } else {
                action = new AlphaAction(startAlpha - fraction * (startAlpha - endAlpha));
            }
            return action;
        }
    }
}
