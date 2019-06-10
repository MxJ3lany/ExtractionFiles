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
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;

import com.lwh.jackknife.widget.R;

import java.util.Calendar;
import java.util.HashMap;
import java.util.regex.Pattern;

public final class DateAdapter extends RecyclerView.Adapter<DateAdapter.ViewHolder>
        implements CalendarView.OnDateClickListener {

    private final int MONTH_IN_YEAR = 12;
    private final int INVALID = -1;
    private final Context mContext;
    private CalendarDay mStartDay;
    private CalendarDay mEndDay;
    private DatePickerController mController;
    private final SelectedDays<CalendarDay> mSelectedDays;
    private boolean mTodaySelected;
    private final int INVALID_COLOR = 0;
    private float mMarkerRadius;
    private int mMarkerColor;
    private int mMarkerTextColor;
    private int mWeekdayTextColor;
    private int mSaturdayTextColor;
    private int mSundayTextColor;
    private int mHeaderHeight;
    private int mHeaderBackgroundColor;
    private int mContentBackgroundColor;
    private int mHeaderTextColor;
    private float mHeaderTextSize;
    private int mEdgeColor;
    private float mDisabledDateAlphaRate;
    private int mDisabledDateAlpha;
    private int mDisabledDateTextColor;
    private boolean mUseCustomDisabledDateTextColor;

    public DateAdapter(Context context, CalendarDay startDay, CalendarDay endDay) {
        this.mContext = context;
        this.mStartDay = startDay;
        this.mEndDay = endDay;
        this.mSelectedDays = new SelectedDays<>();
        init();
    }

    public void setDatePickerViewAttrs(TypedArray ta) {
        parseAttrs(ta);
    }

    public void setDatePickerController(DatePickerController controller) {
        this.mController = controller;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup group, int position) {
        CalendarView calendarView = new CalendarView(mContext);
        return new ViewHolder(calendarView, this);
    }

    private void parseAttrs(TypedArray a) {
        String startDate = a.getString(R.styleable.DatePickerView_dpv_startDate);
        if (isDateValid(startDate)) {
            int startYear = Integer.valueOf(startDate.substring(0, 4));
            int startMonth = Integer.valueOf(startDate.substring(4, 6));
            int startDay = Integer.valueOf(startDate.substring(6));
            mStartDay = new CalendarDay(startYear, startMonth - 1, startDay);
        }
        String endDate = a.getString(R.styleable.DatePickerView_dpv_endDate);
        if (isDateValid(endDate)) {
            int endYear = Integer.valueOf(endDate.substring(0, 4));
            int endMonth = Integer.valueOf(endDate.substring(4, 6));
            int endDay = Integer.valueOf(endDate.substring(6));
            mEndDay = new CalendarDay(endYear, endMonth - 1, endDay);
        }
        mMarkerRadius = a.getDimension(R.styleable.DatePickerView_dpv_markerRadius, 25);
        mMarkerTextColor = a.getColor(R.styleable.DatePickerView_dpv_markerTextColor, INVALID_COLOR);
        mHeaderTextColor = a.getColor(R.styleable.DatePickerView_dpv_headerTextColor, INVALID_COLOR);
        mHeaderTextSize = a.getDimension(R.styleable.DatePickerView_dpv_headerTextSize, 20);
        mHeaderHeight = a.getDimensionPixelSize(R.styleable.DatePickerView_dpv_headerHeight, 75);
        mHeaderBackgroundColor = a.getColor(R.styleable.DatePickerView_dpv_headerBackgroundColor, INVALID_COLOR);
        mContentBackgroundColor = a.getColor(R.styleable.DatePickerView_dpv_contentBackgroundColor, INVALID_COLOR);
        mWeekdayTextColor = a.getColor(R.styleable.DatePickerView_dpv_weekdayTextColor, INVALID_COLOR);
        mSaturdayTextColor = a.getColor(R.styleable.DatePickerView_dpv_saturdayTextColor, INVALID_COLOR);
        mSundayTextColor = a.getColor(R.styleable.DatePickerView_dpv_sundayTextColor, INVALID_COLOR);
        mMarkerColor = a.getColor(R.styleable.DatePickerView_dpv_markerColor, INVALID_COLOR);
        mEdgeColor = a.getColor(R.styleable.DatePickerView_dpv_edgeColor, INVALID_COLOR);
        mUseCustomDisabledDateTextColor = a.getBoolean(R.styleable.DatePickerView_dpv_useCustomDisabledDateTextColor, false);
        mDisabledDateAlphaRate = a.getFraction(R.styleable.DatePickerView_dpv_disabledDateAlphaRate,
                255, 1, 0.5f);
        mDisabledDateAlpha = (int) (mDisabledDateAlphaRate * 255);
        mDisabledDateTextColor = a.getColor(R.styleable.DatePickerView_dpv_disabledDateTextColor, INVALID_COLOR);
        a.recycle();
    }

    private boolean isDateValid(String date) {
        return date != null && Pattern.matches("^\\d{8}$", date);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CalendarView cv = holder.calendarView;
        cv.setStartDay(mStartDay);
        cv.setEndDay(mEndDay);
        cv.setMarkerTextColor(mMarkerTextColor);
        cv.setHeaderHeight(mHeaderHeight);
        cv.setHeaderTextColor(mHeaderTextColor);
        cv.setHeaderTextSize(mHeaderTextSize);
        cv.setHeaderBackgroundColor(mHeaderBackgroundColor);
        cv.setWeekdayTextColor(mWeekdayTextColor);
        cv.setSaturdayTextColor(mSaturdayTextColor);
        cv.setSundayTextColor(mSundayTextColor);
        cv.setHeaderBackgroundColor(mHeaderBackgroundColor);
        cv.setContentBackgroundColor(mContentBackgroundColor);
        cv.setMarkerColor(mMarkerColor);
        cv.setMarkerRadius(mMarkerRadius);
        cv.setEdgeColor(mEdgeColor);
        cv.setDisabledDateAlpha(mDisabledDateAlpha);
        cv.setDisabledDateTextColor(mDisabledDateTextColor);
        cv.setUseCustomDisabledDateTextColor(mUseCustomDisabledDateTextColor);
        HashMap<String, Integer> drawingParams = new HashMap<>();
        int month;
        int year;
        month = (mStartDay.month + (position % MONTH_IN_YEAR)) % MONTH_IN_YEAR;
        year = position / MONTH_IN_YEAR + mStartDay.year + ((mStartDay.month +
                (position % MONTH_IN_YEAR)) / MONTH_IN_YEAR);

        int selectedFirstDay = INVALID;
        int selectedLastDay = INVALID;
        int selectedFirstMonth = INVALID;
        int selectedLastMonth = INVALID;
        int selectedFirstYear = INVALID;
        int selectedLastYear = INVALID;

        if (mSelectedDays.getFirst() != null) {
            selectedFirstDay = mSelectedDays.getFirst().day;
            selectedFirstMonth = mSelectedDays.getFirst().month;
            selectedFirstYear = mSelectedDays.getFirst().year;
        }

        if (mSelectedDays.getLast() != null) {
            selectedLastDay = mSelectedDays.getLast().day;
            selectedLastMonth = mSelectedDays.getLast().month;
            selectedLastYear = mSelectedDays.getLast().year;
        }

        cv.reuse();

        drawingParams.put(CalendarView.DRAWING_PARAMS_SELECTED_BEGIN_YEAR, selectedFirstYear);
        drawingParams.put(CalendarView.DRAWING_PARAMS_SELECTED_LAST_YEAR, selectedLastYear);
        drawingParams.put(CalendarView.DRAWING_PARAMS_SELECTED_BEGIN_MONTH, selectedFirstMonth);
        drawingParams.put(CalendarView.DRAWING_PARAMS_SELECTED_LAST_MONTH, selectedLastMonth);
        drawingParams.put(CalendarView.DRAWING_PARAMS_SELECTED_BEGIN_DAY, selectedFirstDay);
        drawingParams.put(CalendarView.DRAWING_PARAMS_SELECTED_LAST_DAY, selectedLastDay);
        drawingParams.put(CalendarView.DRAWING_PARAMS_YEAR, year);
        drawingParams.put(CalendarView.DRAWING_PARAMS_MONTH, month);
        drawingParams.put(CalendarView.DRAWING_PARAMS_WEEK_START, Calendar.getInstance().getFirstDayOfWeek());
        cv.setDrawingParams(drawingParams);
        cv.invalidate();
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return (mEndDay.year - mStartDay.year) * 12 + (mEndDay.month - mStartDay.month) + 1;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        CalendarView calendarView;

        public ViewHolder(View itemView, CalendarView.OnDateClickListener listener) {
            super(itemView);
            calendarView = (CalendarView) itemView;
            calendarView.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT));
            calendarView.setClickable(true);
            calendarView.setOnDateClickListener(listener);
        }
    }

    public void setTodaySelected(boolean isTodaySelected) {
        this.mTodaySelected = isTodaySelected;
    }

    protected void init() {
        if (mTodaySelected) {
            onDateSelected(new CalendarDay(System.currentTimeMillis()));
        }
    }

    @Override
    public void onDateClick(CalendarView calendarView, CalendarDay calendarDay) {
        if (calendarDay != null) {
            onDateSelected(calendarDay);
        }
    }

    protected void onDateSelected(CalendarDay calendarDay) {
        if (mController != null) {
            mController.onDateSelected(calendarDay.year, calendarDay.month, calendarDay.day);
            setSelectedDay(calendarDay);
        }
    }

    public void setSelectedDay(CalendarDay calendarDay) {
        if (mSelectedDays.getFirst() != null && mSelectedDays.getLast() == null) {
            mSelectedDays.setLast(calendarDay);
            if (mController != null) {
                if (mSelectedDays.getFirst().month < calendarDay.month) {
                    for (int i = 0; i < mSelectedDays.getFirst().month - calendarDay.month - 1; ++i)
                        mController.onDateSelected(mSelectedDays.getFirst().year, mSelectedDays
                                .getFirst().month + i, mSelectedDays.getFirst().day);
                }
                mController.onDateRangeSelected(mSelectedDays);
            }
        } else if (mSelectedDays.getLast() != null) {
            mSelectedDays.setFirst(calendarDay);
            mSelectedDays.setLast(null);
        } else
            mSelectedDays.setFirst(calendarDay);
        notifyDataSetChanged();
    }

    public SelectedDays<CalendarDay> getSelectedDays() {
        return mSelectedDays;
    }
}