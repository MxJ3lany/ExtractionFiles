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
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.List;

public class BottomNavigationBar extends RadioGroup implements View.OnClickListener,
        OnShadeChangeListener {

    private List<ShadeView> mTabButtons;
    private int mLastChecked = -1;

    public BottomNavigationBar(Context context) {
        this(context, null);
    }

    public BottomNavigationBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);
        mTabButtons = new ArrayList<>();
    }

    @Override
    public void onShadeChanged(int position, float ratio) {
        if (ratio > 0 && mTabButtons.size() > 1) {
            ShadeView leftTab = mTabButtons.get(position);
            ShadeView rightTab = mTabButtons.get(position + 1);
            leftTab.setShade(1 - ratio);
            rightTab.setShade(ratio);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        int count = getChildCount();
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                if (child instanceof ShadeView) {
                    ShadeView button = (ShadeView) child;
                    mTabButtons.add(button);
                }
            }
        }
    }

    @Override
    public void check(int id) {
        if (mLastChecked == -1 && mLastChecked == id) {
            return;
        }
        if (id != -1) {
            mLastChecked = id;
        }
        super.check(id);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        LayoutParams lp = (LayoutParams) params;
        lp.width = 0;
        lp.weight = 1;
        child.setOnClickListener(this);
        super.addView(child, index, lp);
    }

    @Override
    public void onClick(View v) {
        resetTabsStatus();
        if (mLastChecked != -1) {
            ShadeView button = mTabButtons.get(mLastChecked);
            button.setChecked(false);
        }
        if (v instanceof ShadeView) {
            ShadeView button = (ShadeView) v;
            button.setChecked(true);
        }
    }

    private void resetTabsStatus() {
        for (int i = 0; i < mTabButtons.size(); i++) {
            mTabButtons.get(i).setShade(0.0f);
        }
    }
}
