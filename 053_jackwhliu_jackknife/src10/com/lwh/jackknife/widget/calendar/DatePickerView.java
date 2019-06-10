/*
 * Copyright (C) 2019 The JackKnife Open Source Project
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

package com.lwh.jackknife.widget.calendar;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.lwh.jackknife.widget.R;

public class DatePickerView extends RecyclerView {

    protected Context mContext;
    protected DateAdapter mAdapter;
    private DatePickerController mController;
    private TypedArray mTypedArray;

    public DatePickerView(Context context) {
        this(context, null);
    }

    public DatePickerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DatePickerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
        this.mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.DatePickerView);
        if (!isInEditMode()) {
            setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            init();
        }
    }

    public void setController(DatePickerController controller) {
        this.mController = controller;
    }

    public void init() {
        setLayoutManager(new LinearLayoutManager(mContext));
        setVerticalScrollBarEnabled(false);
        setFadingEdgeLength(0);
    }

    public void setDateAdapter(DateAdapter adapter) {
        this.mAdapter = adapter;
        adapter.setDatePickerViewAttrs(mTypedArray);
        setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public SelectedDays<CalendarDay> getSelectedDays() {
        return mAdapter.getSelectedDays();
    }

    protected DatePickerController getController() {
        return mController;
    }
}