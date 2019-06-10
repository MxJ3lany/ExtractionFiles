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
import android.widget.ScrollView;

public class AnimatorScrollView extends ScrollView {

    private AnimatorRecycler mContentRecycler;
    private boolean mFirstMatchHeight;

    public AnimatorScrollView(Context context) {
        this(context, null);
    }

    public AnimatorScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.animatorScrollViewStyle);
    }

    public AnimatorScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs, defStyleAttr);
    }

    private void initAttrs(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AnimatorScrollView,
                defStyleAttr, 0);
        mFirstMatchHeight = a.getBoolean(R.styleable
                .AnimatorScrollView_animatorscrollview_firstMatchHeight, false);
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
        if (mFirstMatchHeight) {
            View first = mContentRecycler.getChildAt(0);
            if (first != null) {
                first.getLayoutParams().height = getHeight();
            }
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        int scrollViewHeight = getHeight();
        for (int i = 0; i < mContentRecycler.getChildCount(); i++) {
            View child = mContentRecycler.getChildAt(i);
            if (!(child instanceof AnimatorDragger)) {
                continue;
            }
            AnimatorDragger dragger = (AnimatorDragger) child;
            int wrapperTop = child.getTop();
            int wrapperHeight = child.getHeight();
            int wrapperAbsoluteTop = wrapperTop - t;
            if (wrapperAbsoluteTop <= scrollViewHeight) {
                int visibleGap = scrollViewHeight - wrapperAbsoluteTop;
                dragger.onDrag(clamp(visibleGap / (float) wrapperHeight, 1.0f, 0.0f));
            } else {
                dragger.onReset();
            }
        }
    }

    private static float clamp(float value, float max, float min) {
        return Math.max(Math.min(value, max), min);
    }
}
