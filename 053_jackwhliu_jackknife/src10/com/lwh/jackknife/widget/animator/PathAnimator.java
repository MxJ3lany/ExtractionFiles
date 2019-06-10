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

public class PathAnimator<PA extends PathAction> extends ActionWrapper<PA> {

    private final String PATH = "path";

    public PathAnimator() {
        this(null);
    }

    public PathAnimator(ActionWrapper base) {
        super(base);
    }

    @Override
    public PathAnimator add(PA action) {
        mActionTree.add(action);
        return this;
    }

    @Override
    public void startAnimation(View view, int duration) {
        super.startAnimation(view, duration);
        mTargetView = view;
        mActionTree.add(0, (PA) new MoveTo(0, 0));
        ObjectAnimator animator = ObjectAnimator.ofObject(this, PATH, new PathEvaluator(),
                mActionTree.toArray());
        animator.setDuration(duration);
        animator.start();
    }

    public void setPath(MoveTo action) {
        float x = action.getX();
        float y = action.getY();
        mTargetView.setTranslationX(x);
        mTargetView.setTranslationY(y);
    }

    private class PathEvaluator implements TypeEvaluator<PA> {

        @Override
        public PA evaluate(float fraction, PA startValue, PA endValue) {
            float x = 0;
            float y = 0;
            if (endValue instanceof MoveTo) {
                x = endValue.getX();
                y = endValue.getY();
            }
            if (endValue instanceof LineTo) {
                x = startValue.getX() + fraction * (endValue.getX() - startValue.getX());
                y = startValue.getY() + fraction * (endValue.getY() - startValue.getY());
            }
            float ratio = 1 - fraction;
            if (endValue instanceof QuadTo) {
                x = ((float) Math.pow(ratio, 2)) * startValue.getX() + 2 * fraction * ratio
                        * ((QuadTo) endValue).getInflectionX() + ((float) Math.pow(endValue.getX(), 2))
                        * ((float) Math.pow(fraction, 2));
                y = ((float) Math.pow(ratio, 2)) * startValue.getY() + 2 * fraction * ratio
                        * ((QuadTo) endValue).getInflectionY() + ((float) Math.pow(endValue.getY(), 2))
                        * ((float) Math.pow(fraction, 2));
            }
            if (endValue instanceof CubicTo) {
                x = ((float) (Math.pow(ratio, 3))) * startValue.getX()
                        + 3 * ((float) Math.pow(ratio, 2)) * fraction
                        * ((CubicTo) endValue).getInflectionX1() + 3 * ratio *
                        ((float) Math.pow(fraction, 2))
                        * ((CubicTo) endValue).getInflectionX2() +
                        ((float) Math.pow(fraction, 3)) * endValue.getX();
                y = ((float) (Math.pow(ratio, 3))) * startValue.getY()
                        + 3 * ((float) Math.pow(ratio, 2)) * fraction
                        * ((CubicTo) endValue).getInflectionY1() + 3 * ratio *
                        ((float) Math.pow(fraction, 2))
                        * ((CubicTo) endValue).getInflectionY2() +
                        ((float) Math.pow(fraction, 3)) * endValue.getY();
            }
            return (PA) new MoveTo(x, y);
        }
    }
}
