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

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class AnimatorViewWrapper extends FrameLayout implements AnimatorDragger {

    private static final int TRANSLATION_FROM_LEFT = 0x01;
    private static final int TRANSLATION_FROM_TOP = 0x02;
    private static final int TRANSLATION_FROM_RIGHT = 0x04;
    private static final int TRANSLATION_FROM_BOTTOM = 0x08;
    private int mFromColor;
    private int mToColor;
    private boolean mAlpha;
    private int mTranslation;
    private boolean mScaleX;
    private boolean mScaleY;
    private ArgbEvaluator mEvaluator;
    private int mWidth;
    private int mHeight;

    public AnimatorViewWrapper(Context context) {
        this(context, null);
    }

    public AnimatorViewWrapper(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.animatorViewWrapperStyle);
    }

    public AnimatorViewWrapper(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mEvaluator = new ArgbEvaluator();
    }

    public void setAlpha(boolean alpha) {
        this.mAlpha = alpha;
    }

    public void setFromColor(int color) {
        this.mFromColor = color;
    }

    public void setToColor(int color) {
        this.mToColor = color;
    }

    public void setScaleX(boolean scaleX) {
        this.mScaleX = scaleX;
    }

    public void setScaleY(boolean scaleY) {
        this.mScaleY = scaleY;
    }

    public void setTranslation(int translation) {
        this.mTranslation = translation;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        onReset();
    }

    @Override
    public void onDrag(float ratio) {
        if (mAlpha) {
            setAlpha(ratio);
        }
        if (mScaleX) {
            setScaleX(ratio);
        }
        if (mScaleY) {
            setScaleY(ratio);
        }
        if (isTranslationFrom(TRANSLATION_FROM_BOTTOM)) {
            setTranslationY(mHeight * (1 - ratio));//mHeight-->0(代表原来的位置)
        }
        if (isTranslationFrom(TRANSLATION_FROM_TOP)) {
            setTranslationY(-mHeight * (1 - ratio));//-mHeight-->0(代表原来的位置)
        }
        if (isTranslationFrom(TRANSLATION_FROM_LEFT)) {
            setTranslationX(-mWidth * (1 - ratio));//-width-->0(代表原来的位置)
        }
        if (isTranslationFrom(TRANSLATION_FROM_RIGHT)) {
            setTranslationX(mWidth * (1 - ratio));//width-->0(代表原来的位置)
        }
        if (mFromColor != -1 && mToColor != -1) {
            setBackgroundColor((Integer) mEvaluator.evaluate(ratio, mFromColor, mToColor));
        }
    }

    @Override
    public void onReset() {
        if (mAlpha) {
            setAlpha(0);
        }
        if (mScaleX) {
            setScaleX(0);
        }
        if (mScaleY) {
            setScaleY(0);
        }
        if (isTranslationFrom(TRANSLATION_FROM_TOP)) {
            setTranslationY(-mHeight);
        }
        if (isTranslationFrom(TRANSLATION_FROM_BOTTOM)) {
            setTranslationY(mHeight);
        }
        if (isTranslationFrom(TRANSLATION_FROM_LEFT)) {
            setTranslationX(-mWidth);
        }
        if (isTranslationFrom(TRANSLATION_FROM_RIGHT)) {
            setTranslationX(mWidth);
        }
    }

    private boolean isTranslationFrom(int translationMask) {
        return mTranslation != -1 && (mTranslation & translationMask) == translationMask;
    }
}
