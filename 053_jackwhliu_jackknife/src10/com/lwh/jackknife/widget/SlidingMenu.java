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
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

public class SlidingMenu extends HorizontalScrollView {

    LinearLayout mWrapper;
    ViewGroup mMenu;
    ViewGroup mHost;
    int mScreenWidth;
    int mMenuWidth;
    int mMenuRightPadding;
    float mDownX;

    public SlidingMenu(Context context) {
        this(context, null);
    }

    public SlidingMenu(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.slidingMenuStyle);
    }

    public SlidingMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs, defStyleAttr);
    }

    private void initAttrs(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SlidingMenu, defStyleAttr, 0);
        mMenuRightPadding = a.getDimensionPixelOffset(R.styleable.SlidingMenu_slidingmenu_menuRightPadding, 300);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        View view = getChildAt(0);
        if (view instanceof LinearLayout) {
            mWrapper = (LinearLayout) view;
            mMenu = (ViewGroup) mWrapper.getChildAt(0);
            mHost = (ViewGroup) mWrapper.getChildAt(1);
            WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            DisplayMetrics outMetrics = new DisplayMetrics();
            display.getMetrics(outMetrics);
            mScreenWidth = outMetrics.widthPixels;
            mMenuWidth = mScreenWidth - mMenuRightPadding;
            mMenu.getLayoutParams().width = mMenuWidth;
            mHost.getLayoutParams().width = mScreenWidth;
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed) {
            scrollTo(mMenuWidth, 0);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = ev.getX();
                break;
            case MotionEvent.ACTION_UP:
                if (ev.getX() - mDownX > 0) {
                    smoothScrollTo(0, 0);
                } else {
                    smoothScrollTo(mMenuWidth, 0);
                }
                return true;
        }
        return super.onTouchEvent(ev);
    }
}
