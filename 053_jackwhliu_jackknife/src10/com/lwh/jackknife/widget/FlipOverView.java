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

package com.lwh.jackknife.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class FlipOverView extends ImageView {

    private int mFlipState;
    private int mAnimationDuration = 2000;

    public FlipOverView(Context context) {
        super(context);
    }

    public FlipOverView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FlipOverView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setAnimationDuration(int duration) {
        this.mAnimationDuration = duration;
    }

    public int getAnimationDuration() {
        return mAnimationDuration;
    }

    public void flipOver() {
        if (mFlipState == 0) {
            ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(this, "rotationY", 0, -180f);
            ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(this, "alpha", 1f, 0.2f);
            AnimatorSet set = new AnimatorSet();
            set.playTogether(objectAnimator1, objectAnimator2);
            set.setDuration(mAnimationDuration);
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    setClickable(false);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    setClickable(true);
                }
            });
            set.start();
            mFlipState = 1;
        } else if (mFlipState == 1) {
            ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(this, "rotationY", -180f, 0f);
            ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(this, "alpha", 0.2f, 1f);
            AnimatorSet set = new AnimatorSet();
            set.playTogether(objectAnimator1, objectAnimator2);
            set.setDuration(mAnimationDuration);
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    setClickable(false);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    setClickable(true);
                }
            });
            set.start();
            mFlipState = 2;
        } else if (mFlipState == 2) {
            ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(this, "rotationY", 0f, 180f);
            ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(this, "alpha", 1f, 0.2f);
            AnimatorSet set = new AnimatorSet();
            set.playTogether(objectAnimator1, objectAnimator2);
            set.setDuration(mAnimationDuration);
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    setClickable(false);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    setClickable(true);
                }
            });
            set.start();
            mFlipState = 3;
        } else if (mFlipState == 3) {
            ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(this, "rotationY", 180f, 0f);
            ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(this, "alpha", 0.2f, 1f);
            AnimatorSet set = new AnimatorSet();
            set.playTogether(objectAnimator1, objectAnimator2);
            set.setDuration(mAnimationDuration);
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    setClickable(false);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    setClickable(true);
                }
            });
            set.start();
            mFlipState = 0;
        }
    }
}