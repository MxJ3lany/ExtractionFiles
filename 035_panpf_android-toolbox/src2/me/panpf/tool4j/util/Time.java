/*
 * Copyright (C) 2017 Peng fei Pan <sky@panpf.me>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.panpf.tool4j.util;

import java.util.Calendar;
import java.util.GregorianCalendar;

import me.panpf.tool4j.lang.IntegerUtils;

/**
 * 时间
 */
public class Time {
    private long timeInMillis;
    private int year;
    private int month;
    private int weekOfMonth;
    private int weekOfYear;
    private int dayOfWeek;
    private int dayOfMonth;
    private int dayOfYear;
    private int hour;
    private int amPm;
    private int hourOfDay;
    private int minute;
    private int second;
    private int millisecond;
    private boolean nedUpdate;//是否需要更新

    public Time(long milliseconds) {
        setTimeInMillis(milliseconds);
    }

    public Time() {
        this(System.currentTimeMillis());
    }

    public long getTimeInMillis() {
        if (nedUpdate) {
            update();
        }
        return timeInMillis;
    }

    public void setTimeInMillis(long timeInMillis) {
        this.timeInMillis = timeInMillis;
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(this.timeInMillis);
        this.year = calendar.get(Calendar.YEAR);
        this.month = calendar.get(Calendar.MONTH);
        this.weekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH);
        this.weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
        this.dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        this.dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        this.dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        this.hour = calendar.get(Calendar.HOUR);
        this.amPm = calendar.get(Calendar.AM_PM);
        this.hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        this.minute = calendar.get(Calendar.MINUTE);
        this.second = calendar.get(Calendar.SECOND);
        this.millisecond = calendar.get(Calendar.MILLISECOND);
        nedUpdate = false;
    }

    public int getYear() {
        if (nedUpdate) {
            update();
        }
        return year;
    }

    public void setYear(int year) {
        this.year = year;
        nedUpdate = true;
    }

    public int getMonth() {
        if (nedUpdate) {
            update();
        }
        return month;
    }

    public void setMonth(int month) {
        this.month = month % 12;
        nedUpdate = true;
    }

    public int getWeekOfMonth() {
        if (nedUpdate) {
            update();
        }
        return weekOfMonth;
    }

    public int getWeekOfYear() {
        if (nedUpdate) {
            update();
        }
        return weekOfYear;
    }

    public int getDayOfWeek() {
        if (nedUpdate) {
            update();
        }
        return dayOfWeek;
    }

    public int getDayOfMonth() {
        if (nedUpdate) {
            update();
        }
        return dayOfMonth;
    }

    public void setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth % 32;
        nedUpdate = true;
    }

    public int getDayOfYear() {
        if (nedUpdate) {
            update();
        }
        return dayOfYear;
    }

    public int getHour() {
        if (nedUpdate) {
            update();
        }
        return hour;
    }

    public int getAmPm() {
        if (nedUpdate) {
            update();
        }
        return amPm;
    }

    public int getHourOfDay() {
        if (nedUpdate) {
            update();
        }
        return hourOfDay;
    }

    public void setHourOfDay(int hourOfDay) {
        this.hourOfDay = hourOfDay % 24;
        nedUpdate = true;
    }

    public int getMinute() {
        if (nedUpdate) {
            update();
        }
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute % 60;
        nedUpdate = true;
    }

    public int getSecond() {
        if (nedUpdate) {
            update();
        }
        return second;
    }

    public void setSecond(int second) {
        this.second = second % 60;
        nedUpdate = true;
    }

    public int getMillisecond() {
        if (nedUpdate) {
            update();
        }
        return millisecond;
    }

    public void setMillisecond(int millisecond) {
        this.millisecond = millisecond % 1000;
        nedUpdate = true;
    }

    /**
     * 更新
     */
    public void update() {
        Calendar calendar = new GregorianCalendar(year, month, dayOfMonth, hourOfDay, minute, second);
        calendar.set(Calendar.MILLISECOND, millisecond);
        timeInMillis = calendar.getTimeInMillis();
        weekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH);
        weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
        dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        hour = calendar.get(Calendar.HOUR);
        amPm = calendar.get(Calendar.AM_PM);
        nedUpdate = false;
    }

    public String toStringBy12Hour() {
        return getYear() + "年" + (getMonth() + 1) + "月" + getDayOfMonth() + "日" + " " + "星期" + DateTimeUtils.getWeekChineseName(getDayOfWeek()) + " " + (getAmPm() == Calendar.AM ? "上午" : "下午") + " " + getHour() + "点" + IntegerUtils.fillZero(getMinute(), 2) + "分" + IntegerUtils.fillZero(getSecond(), 2) + "秒" + getMillisecond() + "毫秒，今天是" + (getMonth() + 1) + "月的第" + getWeekOfMonth() + "周，也是" + getYear() + "年的第" + getWeekOfYear() + "周";
    }

    public String toStringBy24Hour() {
        return getYear() + "年" + (getMonth() + 1) + "月" + getDayOfMonth() + "日" + " " + "星期" + DateTimeUtils.getWeekChineseName(getDayOfWeek()) + " " + getHourOfDay() + "点" + IntegerUtils.fillZero(getMinute(), 2) + "分" + IntegerUtils.fillZero(getSecond(), 2) + "秒" + getMillisecond() + "毫秒，今天是" + (getMonth() + 1) + "月的第" + getWeekOfMonth() + "周，也是" + getYear() + "年的第" + getWeekOfYear() + "周";
    }
}