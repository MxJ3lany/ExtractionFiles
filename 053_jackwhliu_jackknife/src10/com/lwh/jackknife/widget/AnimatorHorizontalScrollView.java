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

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.HorizontalScrollView;

public class AnimatorHorizontalScrollView extends HorizontalScrollView {

    private AnimatorRecycler mContentRecycler;
    private boolean mFirstMatchWidth;

    public AnimatorHorizontalScrollView(Context context) {
        this(context, null);
    }

    public AnimatorHorizontalScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.animatorHorizontalScrollViewStyle);
    }

    public AnimatorHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs, defStyleAttr);
    }

    private void initAttrs(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.AnimatorHorizontalScrollView, defStyleAttr, 0);
        mFirstMatchWidth = a.getBoolean(R.styleable
                .AnimatorHorizontalScrollView_animatorhorizontalscrollview_firstMatchWidth, false);
        a.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        View content = getChildAt(0);
        if (content != null && content instanceof AnimatorRecycler) {
            mContentRecycler = (AnimatorRecycler) content;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mFirstMatchWidth) {
            View first = mContentRecycler.getChildAt(0);
            if (first != null) {
                first.getLayoutParams().width = getWidth();
            }
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        int scrollViewWidth = getWidth();
        for (int i = 0; i < mContentRecycler.getChildCount(); i++) {
            View child = mContentRecycler.getChildAt(i);
            if (!(child instanceof AnimatorDragger)) {
                continue;
            }
            AnimatorDragger dragger = (AnimatorDragger) child;
            int wrapperLeft = child.getLeft();
            int wrapperWidth = child.getWidth();
            int wrapperAbsoluteLeft = wrapperLeft - l;
            if (wrapperAbsoluteLeft <= scrollViewWidth) {
                int visibleGap = wrapperWidth - wrapperAbsoluteLeft;
                dragger.onDrag(clamp(visibleGap / (float) wrapperWidth, 1.0f, 0.0f));
            } else {
                dragger.onReset();
            }
        }
    }

    private static float clamp(float value, float max, float min) {
        return Math.max(Math.min(value, max), min);
    }
}
