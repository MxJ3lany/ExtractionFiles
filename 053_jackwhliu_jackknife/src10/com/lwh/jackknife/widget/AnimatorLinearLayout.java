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

package com.lwh.jackknife.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class AnimatorLinearLayout extends LinearLayout implements
        AnimatorRecycler<AnimatorLinearLayout.LayoutParams> {

    public AnimatorLinearLayout(Context context) {
        super(context);
    }

    public AnimatorLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AnimatorLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    public void addView(View child, int index,
                        android.view.ViewGroup.LayoutParams params) {
        LayoutParams lp = (LayoutParams) params;
        if (!applyAnimation(lp)) {
            super.addView(child, index, params);
        } else {
            AnimatorViewWrapper wrapper = new AnimatorViewWrapper(getContext());
            wrapper.setAlpha(lp.mAlpha);
            wrapper.setTranslation(lp.mTranslation);
            wrapper.setScaleX(lp.mScaleX);
            wrapper.setScaleY(lp.mScaleY);
            wrapper.setFromColor(lp.mFromColor);
            wrapper.setToColor(lp.mToColor);
            wrapper.addView(child);
            super.addView(wrapper, index, params);
        }
    }

    @Override
    public boolean applyAnimation(LayoutParams lp) {
        return lp.mAlpha || lp.mScaleX || lp.mScaleY ||
                lp.mTranslation != -1 ||
                (lp.mFromColor != -1 &&
                        lp.mToColor != -1);
    }

    public static class LayoutParams extends LinearLayout.LayoutParams {

        int mFromColor;
        int mToColor;
        boolean mAlpha;
        int mTranslation;
        boolean mScaleX;
        boolean mScaleY;

        public LayoutParams(Context context, AttributeSet attrs) {
            super(context, attrs);
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AnimatorViewWrapper);
            mAlpha = a.getBoolean(R.styleable.AnimatorViewWrapper_animatorviewwrapper_alpha, false);
            mScaleX = a.getBoolean(R.styleable.AnimatorViewWrapper_animatorviewwrapper_scaleX, false);
            mScaleY = a.getBoolean(R.styleable.AnimatorViewWrapper_animatorviewwrapper_scaleY, false);
            mTranslation = a.getInt(R.styleable.AnimatorViewWrapper_animatorviewwrapper_translation, -1);
            mFromColor = a.getColor(R.styleable.AnimatorViewWrapper_animatorviewwrapper_fromColor, -1);
            mToColor = a.getColor(R.styleable.AnimatorViewWrapper_animatorviewwrapper_toColor, -1);
            a.recycle();
        }
    }
}
