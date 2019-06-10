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
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.lwh.jackknife.widget.R;

import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

class CalendarView extends View {

    public static final String DRAWING_PARAMS_MONTH = "month";
    public static final String DRAWING_PARAMS_YEAR = "year";
    public static final String DRAWING_PARAMS_SELECTED_BEGIN_DAY = "selected_begin_day";
    public static final String DRAWING_PARAMS_SELECTED_LAST_DAY = "selected_last_day";
    public static final String DRAWING_PARAMS_SELECTED_BEGIN_MONTH = "selected_begin_month";
    public static final String DRAWING_PARAMS_SELECTED_LAST_MONTH = "selected_last_month";
    public static final String DRAWING_PARAMS_SELECTED_BEGIN_YEAR = "selected_begin_year";
    public static final String DRAWING_PARAMS_SELECTED_LAST_YEAR = "selected_last_year";
    public static final String DRAWING_PARAMS_WEEK_START = "week_start";
    public static final String DRAWING_PARAMS_HEIGHT = "height";
    protected static final int ROW_NUMS = 6;
    protected static int DEFAULT_MARKER_RADIUS;
    protected static int DAY_SEPARATOR_WIDTH = 1;
    protected static int MINI_DAY_NUMBER_TEXT_SIZE;
    protected static int DATE_MIN_HEIGHT = 10;
    protected static int MONTH_DAY_LABEL_TEXT_SIZE;
    protected static int MONTH_HEADER_HEIGHT;
    protected static int MONTH_HEADER_TEXT_SIZE;
    private final int INVALID = -1;
    protected int mPadding = 0;
    protected TextPaint mMonthDatePaint;
    protected Paint mMonthHeaderBgPaint;
    protected TextPaint mMonthHeaderTextPaint;
    protected Paint mSelectedMarkerPaint;
    protected Paint mDateEdgePaint;
    private int mHeaderHeight;
    protected int mHeaderTextColor = Color.BLACK;
    private float mHeaderTextSize;
    protected int mHeaderBackgroundColor = Color.LTGRAY;
    protected int mWeekdayTextColor = Color.BLACK;
    protected int mContentBackgroundColor = Color.WHITE;
    protected int mMarkerColor;
    private int mMarkerTextColor = Color.WHITE;
    protected float mMarkerRadius;
    private int mSaturdayTextColor = Color.BLUE;
    private int mSundayTextColor = Color.RED;
    private int mEdgeColor = Color.BLACK;
    protected boolean mHasToday = false;
    protected boolean mPrev = false;
    protected int mSelectedBeginDay = INVALID;
    protected int mSelectedLastDay = INVALID;
    protected int mSelectedBeginMonth = INVALID;
    protected int mSelectedLastMonth = INVALID;
    protected int mSelectedBeginYear = INVALID;
    protected int mSelectedLastYear = INVALID;
    protected int mToday = INVALID;
    protected int mWeekStart = 1;
    protected int ROW_DAYS = 7;
    protected int mDateNums = ROW_DAYS;
    protected int mMonth;
    /**
     * 行数。
     */
    private int mRowNums = ROW_NUMS;
    protected boolean mDrawRect;
    protected int mRowHeight;
    protected int mWidth;
    protected int mYear;
    private Time mTime;
    private DisplayMetrics mDisplayMetrics;
    private String mMonthHeaderTypeface;
    private int mDayOfWeekStart = 0;
    private Calendar mCalendar;
    private Boolean mPrevDayEnabled;

    private CalendarDay mStartDay;
    private CalendarDay mEndDay;

    private OnDateClickListener mOnDateClickListener;
    private Resources mResources;
    private int mDisabledDateAlpha = 127;
    private int mDisabledDateTextColor;
    private boolean mUseCustomDisabledDateTextColor;

    public CalendarView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        setBackgroundColor(mContentBackgroundColor);
        mResources = context.getResources();
        mCalendar = Calendar.getInstance();
        mTime = new Time(Time.getCurrentTimezone());
        mTime.setToNow();
        mMonthHeaderTypeface = mResources.getString(R.string.sans_serif);

        mDisplayMetrics = mResources.getDisplayMetrics();
        MINI_DAY_NUMBER_TEXT_SIZE = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, mDisplayMetrics);
        MONTH_DAY_LABEL_TEXT_SIZE = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15, mDisplayMetrics);
        MONTH_HEADER_HEIGHT = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, mDisplayMetrics);
        MONTH_HEADER_TEXT_SIZE = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, mDisplayMetrics);
        DEFAULT_MARKER_RADIUS = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, mDisplayMetrics);
        mMarkerRadius = DEFAULT_MARKER_RADIUS;
        mRowHeight = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 270, mDisplayMetrics) - MONTH_HEADER_HEIGHT) / 6;
        mHeaderHeight = MONTH_HEADER_HEIGHT;
        mHeaderTextSize = MONTH_HEADER_TEXT_SIZE;
        mPrevDayEnabled = true;
        initPaints();
    }

    public void setStartDay(CalendarDay startDay) {
        this.mStartDay = startDay;
    }

    public void setEndDay(CalendarDay endDay) {
        this.mEndDay = endDay;
    }

    public void setDrawRect(Boolean isDrawRect) {
        this.mDrawRect = isDrawRect;
    }

    public void setMarkerRadius(float radius) {
        this.mMarkerRadius = radius;
    }

    /**
     * 设置存放日期的容器面板的背景颜色。
     */
    public void setContentBackgroundColor(int color) {
        this.mContentBackgroundColor = color;
    }

    /**
     * 设置日期周一到周五的日期的颜色。
     *
     * @param color 周一到周五的日期的颜色
     */
    public void setWeekdayTextColor(int color) {
        this.mWeekdayTextColor = color;
    }

    public void setMarkerColor(int color) {
        this.mMarkerColor = color;
    }

    public void setHeaderTextColor(int color) {
        this.mHeaderTextColor = color;
    }

    public void setHeaderBackgroundColor(int color) {
        this.mHeaderBackgroundColor = color;
    }

    public void setSaturdayTextColor(int color) {
        this.mSaturdayTextColor = color;
    }

    public void setSundayTextColor(int color) {
        this.mSundayTextColor = color;
    }

    public void setEdgeColor(int color) {
        this.mEdgeColor = color;
    }

    public void setMarkerTextColor(int color) {
        this.mMarkerTextColor = color;
    }

    public void setHeaderHeight(int height) {
        this.mHeaderHeight = height;
    }

    /**
     * 计算需要绘制几行。
     *
     * @return 4~6行
     */
    private int calculateRowNums() {
        int offset = calculateDayOffset();
        int dividend = (offset + mDateNums) / ROW_DAYS;
        int remainder = (offset + mDateNums) % ROW_DAYS;
        return (dividend + (remainder > 0 ? 1 : 0));
    }

    /**
     * 计算日期偏离每行第一个条目的位置偏移量。
     *
     * @return
     */
    private int calculateDayOffset() {
        return (mDayOfWeekStart < mWeekStart ? (mDayOfWeekStart + ROW_DAYS) : mDayOfWeekStart)
                - mWeekStart;
    }

    /**
     * 回调日期被点击事件。
     */
    protected void onDateClick(CalendarDay calendarDay) {
        if (mOnDateClickListener != null && (mPrevDayEnabled || !((calendarDay.month == mTime.month)
                && (calendarDay.year == mTime.year) && calendarDay.day < mTime.monthDay))) {
            mOnDateClickListener.onDateClick(this, calendarDay);
        }
    }

    private boolean checkSameDay(int monthDay, Time time) {
        return (mYear == time.year) && (mMonth == time.month) && (monthDay == time.monthDay);
    }

    private boolean checkPrevDay(int monthDay, Time time) {
        return ((mYear < time.year)) || (mYear == time.year && mMonth < time.month) ||
                (mMonth == time.month && monthDay < time.monthDay);
    }

    /**
     * 绘制月份的标题头，如2019年2月。
     *
     * @param canvas 画布
     */
    private void drawMonthHeader(Canvas canvas) {
        mMonthHeaderBgPaint.setColor(mHeaderBackgroundColor);
        canvas.drawRect(new Rect(0, 0, mWidth, mHeaderHeight), mMonthHeaderBgPaint);
        int x = (mWidth - mPadding) / 2;
        int y = mHeaderHeight / 2;
        mMonthHeaderTextPaint.setColor(mHeaderTextColor);
        mMonthHeaderTextPaint.setTextSize(mHeaderTextSize);
        Paint.FontMetrics fontMetrics = mMonthHeaderTextPaint.getFontMetrics();
        //文本绘制基线，水平穿过文字中央
        float baselineY = y + (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom;
        canvas.drawText(new SimpleDateFormat("yyyy年MM月").format(mCalendar.getTime()),
                x, baselineY, mMonthHeaderTextPaint);
    }

    /**
     * 绘制月份的日期数字。
     *
     * @param canvas 画布
     */
    protected void drawMonthDate(Canvas canvas) {
        int y = (mRowHeight + MINI_DAY_NUMBER_TEXT_SIZE) / 2 - DAY_SEPARATOR_WIDTH + mHeaderHeight;
        //paddingDay，item宽度的一半
        int paddingDay = (mWidth - 2 * mPadding) / (2 * ROW_DAYS);
        int dayOffset = calculateDayOffset();
        int day = 1;
        CalendarDay selectedLastDay = new CalendarDay(mSelectedLastYear, mSelectedLastMonth,
                mSelectedLastDay);
        CalendarDay selectedBeginDay = new CalendarDay(mSelectedBeginYear, mSelectedBeginMonth,
                mSelectedBeginDay);
        while (day <= mDateNums) {
            CalendarDay calendarDay = new CalendarDay(mYear, mMonth, day);
            int x = paddingDay * (1 + dayOffset * 2) + mPadding;
            // 选择的两个点
            if (calendarDay.compareTo(selectedBeginDay) == 0 ||
                    calendarDay.compareTo(selectedLastDay) == 0) {
                mSelectedMarkerPaint.setColor(mMarkerColor);
                if (mDrawRect) {
                    RectF rectF = new RectF(x - mMarkerRadius,
                            (y - MINI_DAY_NUMBER_TEXT_SIZE / 3) - mMarkerRadius,
                            x + mMarkerRadius, (y - MINI_DAY_NUMBER_TEXT_SIZE / 3)
                            + mMarkerRadius);
                    canvas.drawRoundRect(rectF, rectF.centerX(), rectF.centerY(), mSelectedMarkerPaint);
                } else {
                    canvas.drawCircle(x, y - MINI_DAY_NUMBER_TEXT_SIZE / 3,
                            mMarkerRadius, mSelectedMarkerPaint);
                }
            }
            // 今天
            if (mHasToday && (mToday == day)) {
                mMonthDatePaint.setColor(Color.BLACK);
//                mMonthDatePaint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            } else {
                mMonthDatePaint.setColor(mWeekdayTextColor);
                mMonthDatePaint.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            }
            // 设置周日和周六的字体颜色
            if (dayOffset % ROW_DAYS == 0) {
                mMonthDatePaint.setColor(mSundayTextColor);
            }
            if (dayOffset % ROW_DAYS == 6) {
                mMonthDatePaint.setColor(mSaturdayTextColor);
            }
            // 起点或终点
            if (calendarDay.compareTo(selectedBeginDay) == 0 ||
                    calendarDay.compareTo(selectedLastDay) == 0) {
                mMonthDatePaint.setColor(mMarkerTextColor);
            }
            // 起点和终点重叠
            if (calendarDay.compareTo(selectedBeginDay) == 0 &&
                    calendarDay.compareTo(selectedLastDay) == 0) {
                mMonthDatePaint.setColor(mMarkerTextColor);
            }
            if ((selectedBeginDay.isValid() && selectedLastDay.isValid()
                    // 年份相同
                    && mSelectedBeginYear == mSelectedLastYear && mSelectedBeginYear == mYear)
                    && (((mMonth == mSelectedBeginMonth && mSelectedLastMonth ==
                    mSelectedBeginMonth)
                    && ((mSelectedBeginDay < mSelectedLastDay && day >
                    mSelectedBeginDay && day < mSelectedLastDay) || (mSelectedBeginDay >
                    mSelectedLastDay && day < mSelectedBeginDay && day > mSelectedLastDay))) ||
                    ((mSelectedBeginMonth < mSelectedLastMonth && mMonth ==
                            mSelectedBeginMonth && day > mSelectedBeginDay)
                            || (mSelectedBeginMonth < mSelectedLastMonth &&
                            mMonth == mSelectedLastMonth && day < mSelectedLastDay)) ||
                    ((mSelectedBeginMonth > mSelectedLastMonth && mMonth ==
                            mSelectedBeginMonth && day < mSelectedBeginDay) ||
                            (mSelectedBeginMonth > mSelectedLastMonth && mMonth ==
                                    mSelectedLastMonth && day > mSelectedLastDay)))) {
                //选择的两个点之间的
//                mMonthDatePaint.setColor(Color.WHITE);
            }
            if ((selectedBeginDay.isValid() && selectedLastDay.isValid()
                    && mSelectedBeginYear != mSelectedLastYear
                    && ((mSelectedBeginYear == mYear &&
                    mMonth == mSelectedBeginMonth) || (mSelectedLastYear == mYear
                    && mMonth == mSelectedLastMonth)) &&
                    (((mSelectedBeginMonth < mSelectedLastMonth && mMonth == mSelectedBeginMonth
                            && day < mSelectedBeginDay) || (mSelectedBeginMonth < mSelectedLastMonth
                            && mMonth == mSelectedLastMonth && day > mSelectedLastDay)) ||
                            ((mSelectedBeginMonth > mSelectedLastMonth && mMonth
                                    == mSelectedBeginMonth && day > mSelectedBeginDay) ||
                                    (mSelectedBeginMonth > mSelectedLastMonth && mMonth ==
                                            mSelectedLastMonth && day < mSelectedLastDay))))) {
                //跨年的边缘日期
//                mMonthDatePaint.setColor(Color.WHITE);
            }

            if ((mSelectedBeginDay != -1 && mSelectedLastDay != -1 && mSelectedBeginYear
                    == mSelectedLastYear && mYear == mSelectedBeginYear) &&
                    ((mMonth > mSelectedBeginMonth && mMonth < mSelectedLastMonth
                            && mSelectedBeginMonth < mSelectedLastMonth) ||
                            (mMonth < mSelectedBeginMonth && mMonth > mSelectedLastMonth
                                    && mSelectedBeginMonth > mSelectedLastMonth))) {
                //不跨年的中间月
//                mMonthDatePaint.setColor(Color.WHITE);
            }

            if ((mSelectedBeginDay != -1 && mSelectedLastDay != -1 && mSelectedBeginYear != mSelectedLastYear) &&
                    ((mSelectedBeginYear < mSelectedLastYear && ((mMonth > mSelectedBeginMonth
                            && mYear == mSelectedBeginYear) || (mMonth < mSelectedLastMonth
                            && mYear == mSelectedLastYear))) ||
                            (mSelectedBeginYear > mSelectedLastYear && ((mMonth < mSelectedBeginMonth
                                    && mYear == mSelectedBeginYear) || (mMonth > mSelectedLastMonth
                                    && mYear == mSelectedLastYear))))) {
                //跨年的中间月
//                mMonthDatePaint.setColor(Color.WHITE);
            }

            if (isDateOutOfRange(calendarDay)) {
                // 屏蔽不可选择的日期
                if (mUseCustomDisabledDateTextColor) {
                    mMonthDatePaint.setColor(mDisabledDateTextColor);
                } else {
                    mMonthDatePaint.setAlpha(mDisabledDateAlpha);
                }
            }
            canvas.drawText(String.format("%d", day), x, y, mMonthDatePaint);

            dayOffset++;
            if (dayOffset == ROW_DAYS) {
                dayOffset = 0;
                y += mRowHeight;
            }
            day++;
        }
    }

    public boolean isStartDay(CalendarDay calendarDay) {
        CalendarDay startDay = new CalendarDay(mSelectedBeginYear, mSelectedBeginMonth, mSelectedBeginDay);
        return calendarDay.compareTo(startDay) == 0;
    }

    public boolean isBeforeStartDay(CalendarDay calendarDay) {
        CalendarDay startDay = new CalendarDay(mSelectedBeginYear, mSelectedBeginMonth, mSelectedBeginDay);
        return calendarDay.compareTo(startDay) < 0;
    }

    public boolean isBeforeStartMonth(CalendarDay calendarDay) {
        CalendarDay startDay = new CalendarDay(mSelectedBeginYear, mSelectedBeginMonth, mSelectedBeginDay);
        return calendarDay.compareMonth(startDay) < 0;
    }

    public boolean isAfterStartDay(CalendarDay calendarDay) {
        CalendarDay startDay = new CalendarDay(mSelectedBeginYear, mSelectedBeginMonth, mSelectedBeginDay);
        return calendarDay.compareTo(startDay) > 0;
    }

    public boolean isAfterStartMonth(CalendarDay calendarDay) {
        CalendarDay startDay = new CalendarDay(mSelectedBeginYear, mSelectedBeginMonth, mSelectedBeginDay);
        return calendarDay.compareMonth(startDay) > 0;
    }

    public boolean isEndDay(CalendarDay calendarDay) {
        CalendarDay endDay = new CalendarDay(mSelectedLastYear, mSelectedLastMonth, mSelectedLastDay);
        return calendarDay.compareTo(endDay) == 0;
    }

    public boolean isBeforeEndDay(CalendarDay calendarDay) {
        CalendarDay endDay = new CalendarDay(mSelectedLastYear, mSelectedLastMonth, mSelectedLastDay);
        return calendarDay.compareTo(endDay) < 0;
    }

    public boolean isBeforeEndMonth(CalendarDay calendarDay) {
        CalendarDay endDay = new CalendarDay(mSelectedLastYear, mSelectedLastMonth, mSelectedLastDay);
        return calendarDay.compareMonth(endDay) < 0;
    }

    public boolean isAfterEndDay(CalendarDay calendarDay) {
        CalendarDay endDay = new CalendarDay(mSelectedLastYear, mSelectedLastMonth, mSelectedLastDay);
        return calendarDay.compareTo(endDay) > 0;
    }

    public boolean isAfterEndMonth(CalendarDay calendarDay) {
        CalendarDay endDay = new CalendarDay(mSelectedLastYear, mSelectedLastMonth, mSelectedLastDay);
        return calendarDay.compareMonth(endDay) > 0;
    }

    private void drawDateEdge(Canvas canvas) {
        int y = (mRowHeight + MINI_DAY_NUMBER_TEXT_SIZE) / 2 - DAY_SEPARATOR_WIDTH + mHeaderHeight;
        int paddingDay = (mWidth - 2 * mPadding) / (2 * ROW_DAYS);
        int dayOffset = calculateDayOffset();
        int day = 1;
        List<PointF> points = new ArrayList<>();
        PointF firstPoint = null;
        PointF lastPoint = null;
        PointF startPoint;
        PointF endPoint;
        boolean needFirstPoint = false;
        boolean needLastPoint = false;
        CalendarDay selectedLastDay = new CalendarDay(mSelectedLastYear, mSelectedLastMonth,
                mSelectedLastDay);
        CalendarDay selectedBeginDay = new CalendarDay(mSelectedBeginYear, mSelectedBeginMonth,
                mSelectedBeginDay);
        while (day <= mDateNums) {
            int x = paddingDay * (1 + dayOffset * 2) + mPadding;
            if (day == 1) {
                firstPoint = new PointF(x, y);
            }
            if (day == mDateNums) {
                lastPoint = new PointF(x, y);
            }
            if (isStartDay(new CalendarDay(mYear, mMonth, day))) {
                startPoint = new PointF(x, y);
                points.add(startPoint);
                if (selectedBeginDay.isValid() && selectedLastDay
                        .distanceMonth(selectedBeginDay) >= 1) {
                    needLastPoint = true;
                }
                if (selectedLastDay.isValid() && selectedBeginDay
                        .distanceMonth(selectedLastDay) >= 1) {
                    needFirstPoint = true;
                }
            }
            if (isEndDay(new CalendarDay(mYear, mMonth, day))) {
                if (selectedLastDay.isValid() && selectedBeginDay.distanceMonth(selectedLastDay) >= 1) {
                    needLastPoint = true;
                }
                endPoint = new PointF(x, y);
                if (firstPoint != null) {
                    if (selectedBeginDay.isValid() && selectedLastDay
                            .distanceMonth(selectedBeginDay) >= 1) {
                        points.add(firstPoint);
                    }
                }
                points.add(endPoint);
            }
            dayOffset++;
            if (dayOffset == ROW_DAYS) {
                dayOffset = 0;
                y += mRowHeight;
            }
            day++;
        }
        if (needLastPoint && lastPoint != null) {
            points.add(1, lastPoint);
        }
        if (needFirstPoint && firstPoint != null) {
            points.add(0, firstPoint);
        }
        if (((new CalendarDay(mYear, mMonth, day).compareMonth(selectedBeginDay) > 0 &&
                new CalendarDay(mYear, mMonth, day).compareMonth(selectedLastDay) < 0
                && selectedBeginDay.isValid() && selectedBeginDay.compareTo(selectedLastDay) < 0)) ||
                ((new CalendarDay(mYear, mMonth, day).compareMonth(selectedBeginDay) < 0 &&
                        new CalendarDay(mYear, mMonth, day).compareMonth(selectedLastDay) > 0)
                        && selectedLastDay.isValid() && selectedBeginDay.compareTo(selectedLastDay) > 0)) {
            //中间月
            points.add(firstPoint);
            points.add(lastPoint);
        }
        if (points.size() > 1 && points.size() % 2 == 0) {
            for (int i = 0; i < points.size(); i += 2) {
                drawDateEdgeInternal(points.get(i), points.get(i + 1), canvas);
            }
        }
    }

    /**
     * 绘制日期边缘。
     *
     * @param startPoint 开始点
     * @param endPoint 结束点
     * @param canvas 画布
     */
    void drawDateEdgeInternal(PointF startPoint, PointF endPoint, Canvas canvas) {
        mDateEdgePaint.setStrokeWidth(mMarkerRadius * 2);
        mDateEdgePaint.setColor(mEdgeColor);
        int dayWidthHalf = (mWidth - mPadding * 2) / (ROW_DAYS * 2);
        int startX = dayWidthHalf + mPadding;
        int stopX = mWidth - dayWidthHalf - (ROW_DAYS - 1) - mPadding;
        if (endPoint.y > startPoint.y) {
            int row = ((int) Math.abs(endPoint.y - startPoint.y) / mRowHeight + 1);
            if (row > 2) {
                if (startPoint.x == stopX) {
                    mSelectedMarkerPaint.setColor(mEdgeColor);
                    canvas.drawCircle(startPoint.x, startPoint.y - MINI_DAY_NUMBER_TEXT_SIZE / 3,
                            mMarkerRadius, mSelectedMarkerPaint);
                } else {
                    canvas.drawLine(startPoint.x, startPoint.y - MINI_DAY_NUMBER_TEXT_SIZE / 3,
                            stopX, startPoint.y - MINI_DAY_NUMBER_TEXT_SIZE / 3, mDateEdgePaint);
                }
                if (endPoint.x == startX) {
                    mSelectedMarkerPaint.setColor(mEdgeColor);
                    canvas.drawCircle(endPoint.x, endPoint.y - MINI_DAY_NUMBER_TEXT_SIZE / 3,
                            mMarkerRadius, mSelectedMarkerPaint);
                } else {
                    canvas.drawLine(startX, endPoint.y - MINI_DAY_NUMBER_TEXT_SIZE / 3, endPoint.x,
                            endPoint.y - MINI_DAY_NUMBER_TEXT_SIZE / 3, mDateEdgePaint);
                }
                for (int i = 1; i < row - 1; i++) {
                    canvas.drawLine(startX, startPoint.y - MINI_DAY_NUMBER_TEXT_SIZE / 3
                            + mRowHeight * i, stopX, startPoint.y - MINI_DAY_NUMBER_TEXT_SIZE
                            / 3 + mRowHeight * i, mDateEdgePaint);
                }
            } else if (row == 2) {
                if (startPoint.x == stopX) {
                    mSelectedMarkerPaint.setColor(mEdgeColor);
                    canvas.drawCircle(startPoint.x, startPoint.y - MINI_DAY_NUMBER_TEXT_SIZE / 3,
                            mMarkerRadius, mSelectedMarkerPaint);
                } else {
                    canvas.drawLine(startPoint.x, startPoint.y - MINI_DAY_NUMBER_TEXT_SIZE / 3,
                            stopX, startPoint.y - MINI_DAY_NUMBER_TEXT_SIZE / 3, mDateEdgePaint);
                }
                if (endPoint.x == startX) {
                    mSelectedMarkerPaint.setColor(mEdgeColor);
                    canvas.drawCircle(endPoint.x, endPoint.y - MINI_DAY_NUMBER_TEXT_SIZE / 3,
                            mMarkerRadius, mSelectedMarkerPaint);
                } else {
                    canvas.drawLine(startX, endPoint.y - MINI_DAY_NUMBER_TEXT_SIZE / 3,
                            endPoint.x, endPoint.y - MINI_DAY_NUMBER_TEXT_SIZE / 3, mDateEdgePaint);
                }
            }
        } else {
            if (startPoint.x == endPoint.x) {
                mSelectedMarkerPaint.setColor(mEdgeColor);
                canvas.drawCircle(startPoint.x, startPoint.y - MINI_DAY_NUMBER_TEXT_SIZE / 3,
                        mMarkerRadius, mSelectedMarkerPaint);
            } else {
                canvas.drawLine(startPoint.x, startPoint.y - MINI_DAY_NUMBER_TEXT_SIZE / 3,
                        endPoint.x, endPoint.y - MINI_DAY_NUMBER_TEXT_SIZE / 3, mDateEdgePaint);
            }
        }
    }

    /**
     * 获取点击的位置为哪一天。
     */
    protected CalendarDay getDayFromTouchPoint(PointF point) {
        return getDayFromTouchPoint(point.x, point.y);
    }

    /**
     * 获取点击的位置为哪一天。
     */
    protected CalendarDay getDayFromTouchPoint(float x, float y) {
        int padding = mPadding;
        if ((x < padding) || (x > mWidth - mPadding)) {
            return null;
        }
        //yIndex，第几行的日期，0为第1行
        int yIndex = (int) (y - mHeaderHeight) / mRowHeight;
        int day = 1 + ((int) ((x - padding) * ROW_DAYS / (mWidth - padding - mPadding))
                - calculateDayOffset()) + yIndex * ROW_DAYS;
        if (mMonth > 11 || mMonth < 0 || getDaysOfMonth(mYear, mMonth + 1) < day || day < 1) {
            return null;    //返回不了天数的情况
        }
        return new CalendarDay(mYear, mMonth, day);
    }

    /**
     * 检测指定日期是否不可点击。
     */
    public boolean isDateOutOfRange(CalendarDay calendarDay) {
        return calendarDay.compareTo(mStartDay) < 0 || calendarDay.compareTo(mEndDay) > 0;
    }

    protected void initPaints() {

        mMonthHeaderBgPaint = new Paint();
        mMonthHeaderBgPaint.setAntiAlias(true);
        mMonthHeaderBgPaint.setStyle(Style.FILL);
        mMonthHeaderBgPaint.setColor(mHeaderBackgroundColor);

        mMonthHeaderTextPaint = new TextPaint();
        mMonthHeaderTextPaint.setAntiAlias(true);
        mMonthHeaderTextPaint.setTypeface(Typeface.create(mMonthHeaderTypeface, Typeface.BOLD));
        mMonthHeaderTextPaint.setColor(mHeaderTextColor);
        mMonthHeaderTextPaint.setTextSize(mHeaderTextSize);
        mMonthHeaderTextPaint.setTextAlign(Align.CENTER);
        mMonthHeaderTextPaint.setStyle(Style.FILL);

        mSelectedMarkerPaint = new Paint();
        mSelectedMarkerPaint.setFakeBoldText(true);
        mSelectedMarkerPaint.setAntiAlias(true);
        mSelectedMarkerPaint.setColor(mMarkerColor);
        mSelectedMarkerPaint.setTextAlign(Align.CENTER);
        mSelectedMarkerPaint.setStyle(Style.FILL_AND_STROKE);

        mDateEdgePaint = new Paint();
        mDateEdgePaint.setFakeBoldText(true);
        mDateEdgePaint.setAntiAlias(true);
        mDateEdgePaint.setColor(mEdgeColor);
        mDateEdgePaint.setTextAlign(Align.CENTER);
        mDateEdgePaint.setStyle(Style.FILL_AND_STROKE);
        mDateEdgePaint.setStrokeCap(Paint.Cap.ROUND);
        mDateEdgePaint.setStrokeWidth(MINI_DAY_NUMBER_TEXT_SIZE * 2);

        mMonthDatePaint = new TextPaint();
        mMonthDatePaint.setColor(mWeekdayTextColor);
        mMonthDatePaint.setAntiAlias(true);
        mMonthDatePaint.setTextSize(MINI_DAY_NUMBER_TEXT_SIZE);
        mMonthDatePaint.setStyle(Style.FILL);
        mMonthDatePaint.setTextAlign(Align.CENTER);
        mMonthDatePaint.setFakeBoldText(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        setBackgroundColor(mContentBackgroundColor);
        drawMonthHeader(canvas);
        drawDateEdge(canvas);
        drawMonthDate(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        int measuredHeight = (int) (mRowHeight * mRowNums + mHeaderHeight + MINI_DAY_NUMBER_TEXT_SIZE / 3);
        Log.d("onMeasure", "width = " + measuredWidth+" , height = "+ measuredHeight);
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            CalendarDay calendarDay = getDayFromTouchPoint(event.getX(), event.getY());
            if (calendarDay != null && !isDateOutOfRange(calendarDay)) {
                onDateClick(calendarDay);
            }
        }
        return true;
    }

    /**
     * 恢复默认的行数。
     */
    void reuse() {
        mRowNums = ROW_NUMS;
        requestLayout();
    }

    /**
     * 设置绘制时需要用到的参数。
     *
     * @param drawingParams 绘制参数。
     */
    void setDrawingParams(HashMap<String, Integer> drawingParams) {
        if (!drawingParams.containsKey(DRAWING_PARAMS_MONTH) && !drawingParams.containsKey(DRAWING_PARAMS_YEAR)) {
            throw new InvalidParameterException("You must specify month and year for this view");
        }
        setTag(drawingParams);
        if (drawingParams.containsKey(DRAWING_PARAMS_HEIGHT)) {
            mRowHeight = drawingParams.get(DRAWING_PARAMS_HEIGHT);
            if (mRowHeight < DATE_MIN_HEIGHT) {
                mRowHeight = DATE_MIN_HEIGHT;
            }
        }
        if (drawingParams.containsKey(DRAWING_PARAMS_SELECTED_BEGIN_DAY)) {
            mSelectedBeginDay = drawingParams.get(DRAWING_PARAMS_SELECTED_BEGIN_DAY);
        }
        if (drawingParams.containsKey(DRAWING_PARAMS_SELECTED_LAST_DAY)) {
            mSelectedLastDay = drawingParams.get(DRAWING_PARAMS_SELECTED_LAST_DAY);
        }
        if (drawingParams.containsKey(DRAWING_PARAMS_SELECTED_BEGIN_MONTH)) {
            mSelectedBeginMonth = drawingParams.get(DRAWING_PARAMS_SELECTED_BEGIN_MONTH);
        }
        if (drawingParams.containsKey(DRAWING_PARAMS_SELECTED_LAST_MONTH)) {
            mSelectedLastMonth = drawingParams.get(DRAWING_PARAMS_SELECTED_LAST_MONTH);
        }
        if (drawingParams.containsKey(DRAWING_PARAMS_SELECTED_BEGIN_YEAR)) {
            mSelectedBeginYear = drawingParams.get(DRAWING_PARAMS_SELECTED_BEGIN_YEAR);
        }
        if (drawingParams.containsKey(DRAWING_PARAMS_SELECTED_LAST_YEAR)) {
            mSelectedLastYear = drawingParams.get(DRAWING_PARAMS_SELECTED_LAST_YEAR);
        }
        mMonth = drawingParams.get(DRAWING_PARAMS_MONTH);
        mYear = drawingParams.get(DRAWING_PARAMS_YEAR);
        mHasToday = false;
        mToday = -1;
        mCalendar.set(Calendar.MONTH, mMonth);
        mCalendar.set(Calendar.YEAR, mYear);
        mCalendar.set(Calendar.DAY_OF_MONTH, 1);
        mDayOfWeekStart = mCalendar.get(Calendar.DAY_OF_WEEK);
        if (drawingParams.containsKey(DRAWING_PARAMS_WEEK_START)) {
            mWeekStart = drawingParams.get(DRAWING_PARAMS_WEEK_START);
        } else {
            mWeekStart = mCalendar.getFirstDayOfWeek();
        }
        mDateNums = getDaysOfMonth(mYear, mMonth + 1);
        for (int i = 0; i < mDateNums; i++) {
            final int day = i + 1;
            if (checkSameDay(day, mTime)) {
                mHasToday = true;
                mToday = day;
            }
            mPrev = checkPrevDay(day, mTime);
        }
        mRowNums = calculateRowNums();
    }

    /**
     * 判断传入的年份是否为闰年。
     *
     * @param year 年份，如2019
     * @return 是否为闰年，闰年366天，平年365天
     */
    private boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;
    }

    /**
     * 传入年份和月份获取该月份的天数。
     *
     * @param year  年份，如2019
     * @param month 月份，跟实际月份一样，比如一月为1，不为0
     * @return 该月份的天数
     */
    private int getDaysOfMonth(int year, int month) {
        int result;
        switch (month) {
            case 1://1月
            case 3://3月
            case 5://5月
            case 7://7月
            case 8://8月
            case 10://10月
            case 12://12月
                result = 31;
                break;
            case 4://4月
            case 6://6月
            case 9://9月
            case 11://11月
                result = 30;
                break;
            default://2月
                if (isLeapYear(year)) {
                    result = 29;
                } else {
                    result = 28;
                }
                break;
        }
        return result;
    }

    /**
     * 设置日期点击监听器。
     *
     * @param l 日期点击监听器
     */
    public void setOnDateClickListener(OnDateClickListener l) {
        mOnDateClickListener = l;
    }

    public void setHeaderTextSize(float textSize) {
        this.mHeaderTextSize = textSize;
    }

    public void setDisabledDateAlpha(int alpha) {
        this.mDisabledDateAlpha = alpha;
    }

    public void setDisabledDateTextColor(int color) {
        this.mDisabledDateTextColor = color;
    }

    public void setUseCustomDisabledDateTextColor(boolean useCustomDisabledDateTextColor) {
        this.mUseCustomDisabledDateTextColor = useCustomDisabledDateTextColor;
    }

    /**
     * 日期点击监听器。
     */
    public interface OnDateClickListener {
        void onDateClick(CalendarView calendarView, CalendarDay calendarDay);
    }
}