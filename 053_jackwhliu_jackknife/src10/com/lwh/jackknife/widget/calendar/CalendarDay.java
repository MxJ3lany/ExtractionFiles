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

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

public class CalendarDay implements Serializable, Comparable<CalendarDay> {

    private Calendar calendar;
    int day;
    /**
     * 月份，0代表一月。
     */
    int month;
    int year;

    public CalendarDay() {
        set(System.currentTimeMillis());
    }

    public CalendarDay(int year, int month, int day) {
        set(year, month, day);
    }

    public CalendarDay(long timeInMillis) {
        set(timeInMillis);
    }

    public CalendarDay(Calendar calendar) {
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
    }

    private void set(long timeInMillis) {
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }
        calendar.setTimeInMillis(timeInMillis);
        month = this.calendar.get(Calendar.MONTH);
        year = this.calendar.get(Calendar.YEAR);
        day = this.calendar.get(Calendar.DAY_OF_MONTH);
    }

    public void set(CalendarDay calendarDay) {
        year = calendarDay.year;
        month = calendarDay.month;
        day = calendarDay.day;
    }

    public void set(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    /**
     * 是否有效。
     *
     * @return true代表有效，false反之
     */
    public boolean isValid() {
        return year != -1 && month != -1 && day != -1;
    }

    public Date getDate() {
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }
        calendar.set(year, month, day);
        return calendar.getTime();
    }

    @Override
    public String toString() {
        return year+"."+(month+1)+"."+day;
    }

    public int distanceMonth(CalendarDay o) {
        return (year - o.year) * 12 + month - o.month;
    }

    public int compareMonth(CalendarDay o) {
        return (year * 10000 + month * 100) - (o.year * 10000 + o.month * 100);
    }

    @Override
    public int compareTo(@NonNull CalendarDay o) {
        return (year * 10000 + month * 100 + day) - (o.year * 10000 + o.month * 100 + o.day);
    }
}