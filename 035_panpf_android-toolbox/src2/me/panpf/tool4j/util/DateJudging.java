package me.panpf.tool4j.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * 日期判定器，提供了一系列的is方法，用于判定给定日期是昨天还是明天、是上周还是下周、是上个月还是下个月、是去年还是明年
 * <br> 你还有可以使用isAdd()方法来判定其它属性
 */
public class DateJudging {
    private Calendar todayCalendar;

    public DateJudging() {
        todayCalendar = GregorianCalendar.getInstance();
    }

    /**
     * 将当前日期的field属性增加value后同calendar进行比较
     *
     * @param calendar       待比较的日期
     * @param field          要改变的字段
     * @param value          增量
     * @param firstDayOfWeek 一星期的第一天
     * @return 当前日期改变后同date是否一样
     * <br>注意，不同的字段比较的属性也不一样，例如：
     * <br>当field是Calendar.MONTH时将比较Calendar.YEAR和Calendar.MONTH
     * <br>当field是Calendar.DAY_OF_MONTH时将比较Calendar.YEAR、Calendar.MONTH和Calendar.DAY_OF_MONTH
     */
    public boolean isAdd(Calendar calendar, int field, int value, int firstDayOfWeek) {
        if (firstDayOfWeek >= 0) {
            if (todayCalendar.getFirstDayOfWeek() != firstDayOfWeek) {
                todayCalendar.setFirstDayOfWeek(firstDayOfWeek);
                todayCalendar.add(Calendar.MILLISECOND, 1);
                todayCalendar.add(Calendar.MILLISECOND, -1);
            }
            if (calendar.getFirstDayOfWeek() != firstDayOfWeek) {
                calendar.setFirstDayOfWeek(firstDayOfWeek);
                calendar.add(Calendar.MILLISECOND, 1);
                calendar.add(Calendar.MILLISECOND, -1);
            }
        }
        if (value != 0) {
            todayCalendar.add(field, value);
        }
        boolean result = false;
        switch (field) {
            case Calendar.MILLISECOND:
                result = todayCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                        && todayCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
                        && todayCalendar.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)
                        && todayCalendar.get(Calendar.HOUR) == calendar.get(Calendar.HOUR)
                        && todayCalendar.get(Calendar.MINUTE) == calendar.get(Calendar.MINUTE)
                        && todayCalendar.get(Calendar.SECOND) == calendar.get(Calendar.SECOND)
                        && todayCalendar.get(Calendar.MILLISECOND) == calendar.get(Calendar.MILLISECOND);
                break;
            case Calendar.SECOND:
                result = todayCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                        && todayCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
                        && todayCalendar.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)
                        && todayCalendar.get(Calendar.HOUR) == calendar.get(Calendar.HOUR)
                        && todayCalendar.get(Calendar.MINUTE) == calendar.get(Calendar.MINUTE)
                        && todayCalendar.get(Calendar.SECOND) == calendar.get(Calendar.SECOND);
                break;
            case Calendar.MINUTE:
                result = todayCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                        && todayCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
                        && todayCalendar.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)
                        && todayCalendar.get(Calendar.HOUR) == calendar.get(Calendar.HOUR)
                        && todayCalendar.get(Calendar.MINUTE) == calendar.get(Calendar.MINUTE);
                break;
            case Calendar.HOUR:
                result = todayCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                        && todayCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
                        && todayCalendar.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)
                        && todayCalendar.get(Calendar.HOUR) == calendar.get(Calendar.HOUR);
                break;
            case Calendar.HOUR_OF_DAY:
                result = todayCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                        && todayCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
                        && todayCalendar.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)
                        && todayCalendar.get(Calendar.HOUR_OF_DAY) == calendar.get(Calendar.HOUR_OF_DAY);
                break;
            case Calendar.DAY_OF_MONTH:
                result = todayCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                        && todayCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
                        && todayCalendar.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH);
                break;
            case Calendar.DAY_OF_WEEK:
                result = todayCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                        && todayCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
                        && todayCalendar.get(Calendar.WEEK_OF_MONTH) == calendar.get(Calendar.WEEK_OF_MONTH)
                        && todayCalendar.get(Calendar.DAY_OF_WEEK) == calendar.get(Calendar.DAY_OF_WEEK);
                break;
            case Calendar.DAY_OF_WEEK_IN_MONTH:
                result = todayCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                        && todayCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
                        && todayCalendar.get(Calendar.WEEK_OF_MONTH) == calendar.get(Calendar.WEEK_OF_MONTH)
                        && todayCalendar.get(Calendar.DAY_OF_WEEK_IN_MONTH) == calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH);
                break;
            case Calendar.DAY_OF_YEAR:
                result = todayCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                        && todayCalendar.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR);
                break;
            case Calendar.WEEK_OF_MONTH:
                result = todayCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                        && todayCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
                        && todayCalendar.get(Calendar.WEEK_OF_MONTH) == calendar.get(Calendar.WEEK_OF_MONTH);
                break;
            case Calendar.WEEK_OF_YEAR:
                result = todayCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                        && todayCalendar.get(Calendar.WEEK_OF_YEAR) == calendar.get(Calendar.WEEK_OF_YEAR);
                break;
            case Calendar.MONTH:
                result = todayCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                        && todayCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH);
                break;
            case Calendar.YEAR:
                result = todayCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR);
                break;
        }
        if (value != 0) {
            todayCalendar.add(field, -value);
        }
        return result;
    }

    /**
     * 将当前日期的field属性增加value后同calendar进行比较
     *
     * @param calendar 待比较的日期
     * @param field    要改变的字段
     * @param value    增量
     * @return 当前日期改变后同date是否一样
     * <br>注意，不同的字段比较的属性也不一样，例如：
     * <br>当field是Calendar.MONTH时将比较Calendar.YEAR和Calendar.MONTH
     * <br>当field是Calendar.DAY_OF_MONTH时将比较Calendar.YEAR、Calendar.MONTH和Calendar.DAY_OF_MONTH
     */
    public boolean isAdd(Calendar calendar, int field, int value) {
        return isAdd(calendar, field, value, -1);
    }

    /**
     * 将当前日期的field属性增加value后同date进行比较
     *
     * @param date  待比较的日期
     * @param field 要改变的字段
     * @param value 增量
     * @return 当前日期改变后同date是否一样
     * <br>注意，不同的字段比较的属性也不一样，例如：
     * <br>当field是Calendar.MONTH时将比较Calendar.YEAR和Calendar.MONTH
     * <br>当field是Calendar.DAY_OF_MONTH时将比较Calendar.YEAR、Calendar.MONTH和Calendar.DAY_OF_MONTH
     */
    public boolean isAdd(Date date, int field, int value) {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(date);
        return isAdd(calendar, field, value);
    }

    /**
     * 将当前日期的field属性增加value后同date进行比较
     *
     * @param date           待比较的日期
     * @param field          要改变的字段
     * @param value          增量
     * @param firstDayOfWeek 一星期的第一天
     * @return 当前日期改变后同date是否一样
     * <br>注意，不同的字段比较的属性也不一样，例如：
     * <br>当field是Calendar.MONTH时将比较Calendar.YEAR和Calendar.MONTH
     * <br>当field是Calendar.DAY_OF_MONTH时将比较Calendar.YEAR、Calendar.MONTH和Calendar.DAY_OF_MONTH
     */
    public boolean isAdd(Date date, int field, int value, int firstDayOfWeek) {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(date);
        return isAdd(calendar, field, value, firstDayOfWeek);
    }

    /**
     * 将当前日期的field属性增加value后同milliseconds进行比较
     *
     * @param milliseconds 待比较的日期的毫秒值
     * @param field        要改变的字段
     * @param value        增量
     * @return 当前日期改变后同date是否一样
     * <br>注意，不同的字段比较的属性也不一样，例如：
     * <br>当field是Calendar.MONTH时将比较Calendar.YEAR和Calendar.MONTH
     * <br>当field是Calendar.DAY_OF_MONTH时将比较Calendar.YEAR、Calendar.MONTH和Calendar.DAY_OF_MONTH
     */
    public boolean isAdd(long milliseconds, int field, int value) {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        return isAdd(calendar, field, value);
    }

    /**
     * 将当前日期的field属性增加value后同milliseconds进行比较
     *
     * @param milliseconds   待比较的日期的毫秒值
     * @param field          要改变的字段
     * @param value          增量
     * @param firstDayOfWeek 一星期的第一天
     * @return 当前日期改变后同date是否一样
     * <br>注意，不同的字段比较的属性也不一样，例如：
     * <br>当field是Calendar.MONTH时将比较Calendar.YEAR和Calendar.MONTH
     * <br>当field是Calendar.DAY_OF_MONTH时将比较Calendar.YEAR、Calendar.MONTH和Calendar.DAY_OF_MONTH
     */
    public boolean isAdd(long milliseconds, int field, int value, int firstDayOfWeek) {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        return isAdd(calendar, field, value, firstDayOfWeek);
    }

    /**
     * 将当前日期的field属性增加value后同formattedDate进行比较
     *
     * @param formattedDate 已格式化的日期字符串，例如：2014-05-09 10:14
     * @param dateFormat    formattedDate的格式，例如：2014-05-09 10:14的格式为yyyy-MM-dd HH:mm
     * @param field         要改变的字段
     * @param value         增量
     * @return 当前日期改变后同formattedDate是否一样
     * <br>注意，不同的字段比较的属性也不一样，例如：
     * <br>当field是Calendar.MONTH时将比较Calendar.YEAR和Calendar.MONTH
     * <br>当field是Calendar.DAY_OF_MONTH时将比较Calendar.YEAR、Calendar.MONTH和Calendar.DAY_OF_MONTH
     * @throws java.text.ParseException formattedDate的格式和dateFormat所表示的格式不对照
     */
    public boolean isAdd(String formattedDate, String dateFormat, int field, int value) throws ParseException {
        return isAdd(buildCalenderByParseFormattedDate(formattedDate, dateFormat), field, value);
    }

    /**
     * 将当前日期的field属性增加value后同formattedDate进行比较
     *
     * @param formattedDate  已格式化的日期字符串，例如：2014-05-09 10:14
     * @param dateFormat     formattedDate的格式，例如：2014-05-09 10:14的格式为yyyy-MM-dd HH:mm
     * @param field          要改变的字段
     * @param value          增量
     * @param firstDayOfWeek 一星期的第一天
     * @return 当前日期改变后同formattedDate是否一样
     * <br>注意，不同的字段比较的属性也不一样，例如：
     * <br>当field是Calendar.MONTH时将比较Calendar.YEAR和Calendar.MONTH
     * <br>当field是Calendar.DAY_OF_MONTH时将比较Calendar.YEAR、Calendar.MONTH和Calendar.DAY_OF_MONTH
     * @throws java.text.ParseException formattedDate的格式和dateFormat所表示的格式不对照
     */
    public boolean isAdd(String formattedDate, String dateFormat, int field, int value, int firstDayOfWeek) throws ParseException {
        return isAdd(buildCalenderByParseFormattedDate(formattedDate, dateFormat), field, value, firstDayOfWeek);
    }

    /**
     * 判断给定的日历对象是否是今天
     *
     * @param calendar 待判定的日历对象
     * @return true：是今天；false：不是今天
     */
    public boolean isToday(Calendar calendar) {
        return isAdd(calendar, Calendar.DAY_OF_MONTH, 0);
    }

    /**
     * 判断给定的日期对象是否是今天
     *
     * @param date 待判定的日期对象
     * @return true：是今天；false：不是今天
     */
    public boolean isToday(Date date) {
        return isAdd(date, Calendar.DAY_OF_MONTH, 0);
    }

    /**
     * 判断给定的时间毫秒值是否是今天
     *
     * @param milliseconds 待判定的时间的毫秒值
     * @return true：是今天；false：不是今天
     */
    public boolean isToday(long milliseconds) {
        return isAdd(milliseconds, Calendar.DAY_OF_MONTH, 0);
    }

    /**
     * 给定的calendar是否是今天
     *
     * @param formattedDate 已格式化的日期字符串，例如：2014-05-09 10:14
     * @param dateFormat    formattedDate的格式，例如：2014-05-09 10:14的格式为yyyy-MM-dd HH:mm
     * @return true：是今天；false：不是今天
     * @throws java.text.ParseException formattedDate的格式和dateFormat所表示的格式不对照
     */
    public boolean isToday(String formattedDate, String dateFormat) throws ParseException {
        return isAdd(formattedDate, dateFormat, Calendar.DAY_OF_MONTH, 0);
    }

    /**
     * 判断给定的日历对象是否是昨天
     *
     * @param calendar 待判定的日历对象
     * @return true：是昨天；false：不是昨天
     */
    public boolean isYesterday(Calendar calendar) {
        return isAdd(calendar, Calendar.DAY_OF_MONTH, -1);
    }

    /**
     * 判断给定的日期对象是否是昨天
     *
     * @param date 待判定的日期对象
     * @return true：是昨天；false：不是昨天
     */
    public boolean isYesterday(Date date) {
        return isAdd(date, Calendar.DAY_OF_MONTH, -1);
    }

    /**
     * 判断给定的时间毫秒值是否是昨天
     *
     * @param milliseconds 待判定的时间的毫秒值
     * @return true：是昨天；false：不是昨天
     */
    public boolean isYesterday(long milliseconds) {
        return isAdd(milliseconds, Calendar.DAY_OF_MONTH, -1);
    }

    /**
     * 给定的calendar是否是昨天
     *
     * @param formattedDate 已格式化的日期字符串，例如：2014-05-09 10:14
     * @param dateFormat    formattedDate的格式，例如：2014-05-09 10:14的格式为yyyy-MM-dd HH:mm
     * @return true：是昨天；false：不是昨天
     * @throws java.text.ParseException formattedDate的格式和dateFormat所表示的格式不对照
     */
    public boolean isYesterday(String formattedDate, String dateFormat) throws ParseException {
        return isAdd(formattedDate, dateFormat, Calendar.DAY_OF_MONTH, -1);
    }

    /**
     * 判断给定的日历对象是否是明天
     *
     * @param calendar 待判定的日历对象
     * @return true：是明天；false：不是明天
     */
    public boolean isTomorrow(Calendar calendar) {
        return isAdd(calendar, Calendar.DAY_OF_MONTH, 1);
    }

    /**
     * 判断给定的日期对象是否是明天
     *
     * @param date 待判定的日期对象
     * @return true：是明天；false：不是明天
     */
    public boolean isTomorrow(Date date) {
        return isAdd(date, Calendar.DAY_OF_MONTH, 1);
    }

    /**
     * 判断给定的时间毫秒值是否是明天
     *
     * @param milliseconds 待判定的时间的毫秒值
     * @return true：是明天；false：不是明天
     */
    public boolean isTomorrow(long milliseconds) {
        return isAdd(milliseconds, Calendar.DAY_OF_MONTH, 1);
    }

    /**
     * 给定的calendar是否是明天
     *
     * @param formattedDate 已格式化的日期字符串，例如：2014-05-09 10:14
     * @param dateFormat    formattedDate的格式，例如：2014-05-09 10:14的格式为yyyy-MM-dd HH:mm
     * @return true：是明天；false：不是明天
     * @throws java.text.ParseException formattedDate的格式和dateFormat所表示的格式不对照
     */
    public boolean isTomorrow(String formattedDate, String dateFormat) throws ParseException {
        return isAdd(formattedDate, dateFormat, Calendar.DAY_OF_MONTH, 1);
    }

    /**
     * 判断给定的日历对象是否是本周
     *
     * @param calendar 待判定的日历对象
     * @return true：是本周；false：不是本周
     */
    public boolean isThisWeek(Calendar calendar) {
        return isAdd(calendar, Calendar.WEEK_OF_YEAR, 0);
    }

    /**
     * 判断给定的日历对象是否是本周
     *
     * @param calendar       待判定的日历对象
     * @param firstDayOfWeek 一星期的第一天
     * @return true：是本周；false：不是本周
     */
    public boolean isThisWeek(Calendar calendar, int firstDayOfWeek) {
        return isAdd(calendar, Calendar.WEEK_OF_YEAR, 0, firstDayOfWeek);
    }

    /**
     * 判断给定的日期对象是否是本周
     *
     * @param date 待判定的日期对象
     * @return true：是本周；false：不是本周
     */
    public boolean isThisWeek(Date date) {
        return isAdd(date, Calendar.WEEK_OF_YEAR, 0);
    }

    /**
     * 判断给定的日期对象是否是本周
     *
     * @param date           待判定的日期对象
     * @param firstDayOfWeek 一星期的第一天
     * @return true：是本周；false：不是本周
     */
    public boolean isThisWeek(Date date, int firstDayOfWeek) {
        return isAdd(date, Calendar.WEEK_OF_YEAR, 0, firstDayOfWeek);
    }

    /**
     * 判断给定的时间毫秒值是否是本周
     *
     * @param milliseconds 待判定的时间的毫秒值
     * @return true：是本周；false：不是本周
     */
    public boolean isThisWeek(long milliseconds) {
        return isAdd(milliseconds, Calendar.WEEK_OF_YEAR, 0);
    }

    /**
     * 判断给定的时间毫秒值是否是本周
     *
     * @param milliseconds   待判定的时间的毫秒值
     * @param firstDayOfWeek 一星期的第一天
     * @return true：是本周；false：不是本周
     */
    public boolean isThisWeek(long milliseconds, int firstDayOfWeek) {
        return isAdd(milliseconds, Calendar.WEEK_OF_YEAR, 0, firstDayOfWeek);
    }

    /**
     * 给定的calendar是否是本周
     *
     * @param formattedDate 已格式化的日期字符串，例如：2014-05-09 10:14
     * @param dateFormat    formattedDate的格式，例如：2014-05-09 10:14的格式为yyyy-MM-dd HH:mm
     * @return true：是本周；false：不是本周
     * @throws java.text.ParseException formattedDate的格式和dateFormat所表示的格式不对照
     */
    public boolean isThisWeek(String formattedDate, String dateFormat) throws ParseException {
        return isAdd(formattedDate, dateFormat, Calendar.WEEK_OF_YEAR, 0);
    }

    /**
     * 给定的calendar是否是本周
     *
     * @param formattedDate  已格式化的日期字符串，例如：2014-05-09 10:14
     * @param dateFormat     formattedDate的格式，例如：2014-05-09 10:14的格式为yyyy-MM-dd HH:mm
     * @param firstDayOfWeek 一星期的第一天
     * @return true：是本周；false：不是本周
     * @throws java.text.ParseException formattedDate的格式和dateFormat所表示的格式不对照
     */
    public boolean isThisWeek(String formattedDate, String dateFormat, int firstDayOfWeek) throws ParseException {
        return isAdd(formattedDate, dateFormat, Calendar.WEEK_OF_YEAR, 0, firstDayOfWeek);
    }

    /**
     * 判断给定的日历对象是否是上周
     *
     * @param calendar 待判定的日历对象
     * @return true：是上周；false：不是上周
     */
    public boolean isLastWeek(Calendar calendar) {
        return isAdd(calendar, Calendar.WEEK_OF_YEAR, -1);
    }

    /**
     * 判断给定的日历对象是否是上周
     *
     * @param calendar       待判定的日历对象
     * @param firstDayOfWeek 一星期的第一天
     * @return true：是上周；false：不是上周
     */
    public boolean isLastWeek(Calendar calendar, int firstDayOfWeek) {
        return isAdd(calendar, Calendar.WEEK_OF_YEAR, -1, firstDayOfWeek);
    }

    /**
     * 判断给定的日期对象是否是上周
     *
     * @param date 待判定的日期对象
     * @return true：是上周；false：不是上周
     */
    public boolean isLastWeek(Date date) {
        return isAdd(date, Calendar.WEEK_OF_YEAR, -1);
    }

    /**
     * 判断给定的日期对象是否是上周
     *
     * @param date           待判定的日期对象
     * @param firstDayOfWeek 一星期的第一天
     * @return true：是上周；false：不是上周
     */
    public boolean isLastWeek(Date date, int firstDayOfWeek) {
        return isAdd(date, Calendar.WEEK_OF_YEAR, -1, firstDayOfWeek);
    }

    /**
     * 判断给定的时间毫秒值是否是上周
     *
     * @param milliseconds 待判定的时间的毫秒值
     * @return true：是上周；false：不是上周
     */
    public boolean isLastWeek(long milliseconds) {
        return isAdd(milliseconds, Calendar.WEEK_OF_YEAR, -1);
    }

    /**
     * 判断给定的时间毫秒值是否是上周
     *
     * @param milliseconds   待判定的时间的毫秒值
     * @param firstDayOfWeek 一星期的第一天
     * @return true：是上周；false：不是上周
     */
    public boolean isLastWeek(long milliseconds, int firstDayOfWeek) {
        return isAdd(milliseconds, Calendar.WEEK_OF_YEAR, -1, firstDayOfWeek);
    }

    /**
     * 给定的calendar是否是上周
     *
     * @param formattedDate 已格式化的日期字符串，例如：2014-05-09 10:14
     * @param dateFormat    formattedDate的格式，例如：2014-05-09 10:14的格式为yyyy-MM-dd HH:mm
     * @return true：是上周；false：不是上周
     * @throws java.text.ParseException formattedDate的格式和dateFormat所表示的格式不对照
     */
    public boolean isLastWeek(String formattedDate, String dateFormat) throws ParseException {
        return isAdd(formattedDate, dateFormat, Calendar.WEEK_OF_YEAR, -1);
    }

    /**
     * 给定的calendar是否是上周
     *
     * @param formattedDate  已格式化的日期字符串，例如：2014-05-09 10:14
     * @param dateFormat     formattedDate的格式，例如：2014-05-09 10:14的格式为yyyy-MM-dd HH:mm
     * @param firstDayOfWeek 一星期的第一天
     * @return true：是上周；false：不是上周
     * @throws java.text.ParseException formattedDate的格式和dateFormat所表示的格式不对照
     */
    public boolean isLastWeek(String formattedDate, String dateFormat, int firstDayOfWeek) throws ParseException {
        return isAdd(formattedDate, dateFormat, Calendar.WEEK_OF_YEAR, -1, firstDayOfWeek);
    }

    /**
     * 判断给定的日历对象是否是下周
     *
     * @param calendar 待判定的日历对象
     * @return true：是下周；false：不是下周
     */
    public boolean isNextWeek(Calendar calendar) {
        return isAdd(calendar, Calendar.WEEK_OF_YEAR, 1);
    }

    /**
     * 判断给定的日历对象是否是下周
     *
     * @param calendar       待判定的日历对象
     * @param firstDayOfWeek 一星期的第一天
     * @return true：是下周；false：不是下周
     */
    public boolean isNextWeek(Calendar calendar, int firstDayOfWeekr) {
        return isAdd(calendar, Calendar.WEEK_OF_YEAR, 1, firstDayOfWeekr);
    }

    /**
     * 判断给定的日期对象是否是下周
     *
     * @param date 待判定的日期对象
     * @return true：是下周；false：不是下周
     */
    public boolean isNextWeek(Date date) {
        return isAdd(date, Calendar.WEEK_OF_YEAR, 1);
    }

    /**
     * 判断给定的日期对象是否是下周
     *
     * @param date           待判定的日期对象
     * @param firstDayOfWeek 一星期的第一天
     * @return true：是下周；false：不是下周
     */
    public boolean isNextWeek(Date date, int firstDayOfWeek) {
        return isAdd(date, Calendar.WEEK_OF_YEAR, 1, firstDayOfWeek);
    }

    /**
     * 判断给定的时间毫秒值是否是下周
     *
     * @param milliseconds 待判定的时间的毫秒值
     * @return true：是下周；false：不是下周
     */
    public boolean isNextWeek(long milliseconds) {
        return isAdd(milliseconds, Calendar.WEEK_OF_YEAR, 1);
    }

    /**
     * 判断给定的时间毫秒值是否是下周
     *
     * @param milliseconds   待判定的时间的毫秒值
     * @param firstDayOfWeek 一星期的第一天
     * @return true：是下周；false：不是下周
     */
    public boolean isNextWeek(long milliseconds, int firstDayOfWeek) {
        return isAdd(milliseconds, Calendar.WEEK_OF_YEAR, 1, firstDayOfWeek);
    }

    /**
     * 给定的calendar是否是下周
     *
     * @param formattedDate 已格式化的日期字符串，例如：2014-05-09 10:14
     * @param dateFormat    formattedDate的格式，例如：2014-05-09 10:14的格式为yyyy-MM-dd HH:mm
     * @return true：是下周；false：不是下周
     * @throws java.text.ParseException formattedDate的格式和dateFormat所表示的格式不对照
     */
    public boolean isNextWeek(String formattedDate, String dateFormat) throws ParseException {
        return isAdd(formattedDate, dateFormat, Calendar.WEEK_OF_YEAR, 1);
    }

    /**
     * 给定的calendar是否是下周
     *
     * @param formattedDate  已格式化的日期字符串，例如：2014-05-09 10:14
     * @param dateFormat     formattedDate的格式，例如：2014-05-09 10:14的格式为yyyy-MM-dd HH:mm
     * @param firstDayOfWeek 一星期的第一天
     * @return true：是下周；false：不是下周
     * @throws java.text.ParseException formattedDate的格式和dateFormat所表示的格式不对照
     */
    public boolean isNextWeek(String formattedDate, String dateFormat, int firstDayOfWeek) throws ParseException {
        return isAdd(formattedDate, dateFormat, Calendar.WEEK_OF_YEAR, 1, firstDayOfWeek);
    }

    /**
     * 判断给定的日历对象是否是本月
     *
     * @param calendar 待判定的日历对象
     * @return true：是本月；false：不是本月
     */
    public boolean isThisMonth(Calendar calendar) {
        return isAdd(calendar, Calendar.MONTH, 0);
    }

    /**
     * 判断给定的日期对象是否是本月
     *
     * @param date 待判定的日期对象
     * @return true：是本月；false：不是本月
     */
    public boolean isThisMonth(Date date) {
        return isAdd(date, Calendar.MONTH, 0);
    }

    /**
     * 判断给定的时间毫秒值是否是本月
     *
     * @param milliseconds 待判定的时间的毫秒值
     * @return true：是本月；false：不是本月
     */
    public boolean isThisMonth(long milliseconds) {
        return isAdd(milliseconds, Calendar.MONTH, 0);
    }

    /**
     * 给定的calendar是否是本月
     *
     * @param formattedDate 已格式化的日期字符串，例如：2014-05-09 10:14
     * @param dateFormat    formattedDate的格式，例如：2014-05-09 10:14的格式为yyyy-MM-dd HH:mm
     * @return true：是本月；false：不是本月
     * @throws java.text.ParseException formattedDate的格式和dateFormat所表示的格式不对照
     */
    public boolean isThisMonth(String formattedDate, String dateFormat) throws ParseException {
        return isAdd(formattedDate, dateFormat, Calendar.MONTH, 0);
    }

    /**
     * 判断给定的日历对象是否是上个月
     *
     * @param calendar 待判定的日历对象
     * @return true：是上个月；false：不是上个月
     */
    public boolean isLastMonth(Calendar calendar) {
        return isAdd(calendar, Calendar.MONTH, -1);
    }

    /**
     * 判断给定的日期对象是否是上个月
     *
     * @param date 待判定的日期对象
     * @return true：是上个月；false：不是上个月
     */
    public boolean isLastMonth(Date date) {
        return isAdd(date, Calendar.MONTH, -1);
    }

    /**
     * 判断给定的时间毫秒值是否是上个月
     *
     * @param milliseconds 待判定的时间的毫秒值
     * @return true：是上个月；false：不是上个月
     */
    public boolean isLastMonth(long milliseconds) {
        return isAdd(milliseconds, Calendar.MONTH, -1);
    }

    /**
     * 给定的calendar是否是上个月
     *
     * @param formattedDate 已格式化的日期字符串，例如：2014-05-09 10:14
     * @param dateFormat    formattedDate的格式，例如：2014-05-09 10:14的格式为yyyy-MM-dd HH:mm
     * @return true：是上个月；false：不是上个月
     * @throws java.text.ParseException formattedDate的格式和dateFormat所表示的格式不对照
     */
    public boolean isLastMonth(String formattedDate, String dateFormat) throws ParseException {
        return isAdd(formattedDate, dateFormat, Calendar.MONTH, -1);
    }

    /**
     * 判断给定的日历对象是否是下个月
     *
     * @param calendar 待判定的日历对象
     * @return true：是下个月；false：不是下个月
     */
    public boolean isNextMonth(Calendar calendar) {
        return isAdd(calendar, Calendar.MONTH, 1);
    }

    /**
     * 判断给定的日期对象是否是下个月
     *
     * @param date 待判定的日期对象
     * @return true：是下个月；false：不是下个月
     */
    public boolean isNextMonth(Date date) {
        return isAdd(date, Calendar.MONTH, 1);
    }

    /**
     * 判断给定的时间毫秒值是否是下个月
     *
     * @param milliseconds 待判定的时间的毫秒值
     * @return true：是下个月；false：不是下个月
     */
    public boolean isNextMonth(long milliseconds) {
        return isAdd(milliseconds, Calendar.MONTH, 1);
    }

    /**
     * 给定的calendar是否是下个月
     *
     * @param formattedDate 已格式化的日期字符串，例如：2014-05-09 10:14
     * @param dateFormat    formattedDate的格式，例如：2014-05-09 10:14的格式为yyyy-MM-dd HH:mm
     * @return true：是下个月；false：不是下个月
     * @throws java.text.ParseException formattedDate的格式和dateFormat所表示的格式不对照
     */
    public boolean isNextMonth(String formattedDate, String dateFormat) throws ParseException {
        return isAdd(formattedDate, dateFormat, Calendar.MONTH, 1);
    }

    /**
     * 判断给定的日历对象是否是本年
     *
     * @param calendar 待判定的日历对象
     * @return true：是本年；false：不是本年
     */
    public boolean isThisYear(Calendar calendar) {
        return isAdd(calendar, Calendar.YEAR, 0);
    }

    /**
     * 判断给定的日期对象是否是本年
     *
     * @param date 待判定的日期对象
     * @return true：是本年；false：不是本年
     */
    public boolean isThisYear(Date date) {
        return isAdd(date, Calendar.YEAR, 0);
    }

    /**
     * 判断给定的时间毫秒值是否是本年
     *
     * @param milliseconds 待判定的时间的毫秒值
     * @return true：是本年；false：不是本年
     */
    public boolean isThisYear(long milliseconds) {
        return isAdd(milliseconds, Calendar.YEAR, 0);
    }

    /**
     * 给定的calendar是否是本年
     *
     * @param formattedDate 已格式化的日期字符串，例如：2014-05-09 10:14
     * @param dateFormat    formattedDate的格式，例如：2014-05-09 10:14的格式为yyyy-MM-dd HH:mm
     * @return true：是本年；false：不是本年
     * @throws java.text.ParseException formattedDate的格式和dateFormat所表示的格式不对照
     */
    public boolean isThisYear(String formattedDate, String dateFormat) throws ParseException {
        return isAdd(formattedDate, dateFormat, Calendar.YEAR, 0);
    }

    /**
     * 判断给定的日历对象是否是去年
     *
     * @param calendar 待判定的日历对象
     * @return true：是去年；false：不是去年
     */
    public boolean isLastYear(Calendar calendar) {
        return isAdd(calendar, Calendar.YEAR, -1);
    }

    /**
     * 判断给定的日期对象是否是去年
     *
     * @param date 待判定的日期对象
     * @return true：是去年；false：不是去年
     */
    public boolean isLastYear(Date date) {
        return isAdd(date, Calendar.YEAR, -1);
    }

    /**
     * 判断给定的时间毫秒值是否是去年
     *
     * @param milliseconds 待判定的时间的毫秒值
     * @return true：是去年；false：不是去年
     */
    public boolean isLastYear(long milliseconds) {
        return isAdd(milliseconds, Calendar.YEAR, -1);
    }

    /**
     * 给定的calendar是否是去年
     *
     * @param formattedDate 已格式化的日期字符串，例如：2014-05-09 10:14
     * @param dateFormat    formattedDate的格式，例如：2014-05-09 10:14的格式为yyyy-MM-dd HH:mm
     * @return true：是去年；false：不是去年
     * @throws java.text.ParseException formattedDate的格式和dateFormat所表示的格式不对照
     */
    public boolean isLastYear(String formattedDate, String dateFormat) throws ParseException {
        return isAdd(formattedDate, dateFormat, Calendar.YEAR, -1);
    }

    /**
     * 判断给定的日历对象是否是明年
     *
     * @param calendar 待判定的日历对象
     * @return true：是明年；false：不是明年
     */
    public boolean isNextYear(Calendar calendar) {
        return isAdd(calendar, Calendar.YEAR, 1);
    }

    /**
     * 判断给定的日期对象是否是明年
     *
     * @param date 待判定的日期对象
     * @return true：是明年；false：不是明年
     */
    public boolean isNextYear(Date date) {
        return isAdd(date, Calendar.YEAR, 1);
    }

    /**
     * 判断给定的时间毫秒值是否是明年
     *
     * @param milliseconds 待判定的时间的毫秒值
     * @return true：是明年；false：不是明年
     */
    public boolean isNextYear(long milliseconds) {
        return isAdd(milliseconds, Calendar.YEAR, 1);
    }

    /**
     * 给定的calendar是否是明年
     *
     * @param formattedDate 已格式化的日期字符串，例如：2014-05-09 10:14
     * @param dateFormat    formattedDate的格式，例如：2014-05-09 10:14的格式为yyyy-MM-dd HH:mm
     * @return true：是明年；false：不是明年
     * @throws java.text.ParseException formattedDate的格式和dateFormat所表示的格式不对照
     */
    public boolean isNextYear(String formattedDate, String dateFormat) throws ParseException {
        return isAdd(formattedDate, dateFormat, Calendar.YEAR, 1);
    }

    /**
     * 获取日历对象
     *
     * @return 日历对象
     */
    public Calendar getCalendar() {
        return todayCalendar;
    }

    /**
     * 通过解析一个已格式化的日期字符串来创建一个Date对象
     *
     * @param formattedDate 已格式化的日期字符串，例如：2014-05-09 10:14
     * @param dateFormat    formattedDate的格式，例如：2014-05-09 10:14的格式为yyyy-MM-dd HH:mm
     * @return 一个Date对象
     * @throws java.text.ParseException formattedDate的格式和dateFormat所表示的格式不对照
     */
    public static Date buildDateByParseFormattedDate(String formattedDate, String dateFormat) throws ParseException {
        return new SimpleDateFormat(dateFormat, Locale.getDefault()).parse(formattedDate);
    }

    /**
     * 通过解析一个已格式化的日期字符串来创建一个Calendar对象
     *
     * @param formattedDate 已格式化的日期字符串，例如：2014-05-09 10:14
     * @param dateFormat    日期的格式，例如：2014-05-09 10:14的格式为yyyy-MM-dd HH:mm
     * @return 一个Calendar对象
     * @throws java.text.ParseException formattedDate的格式和dateFormat所表示的格式不对照
     */
    public static Calendar buildCalenderByParseFormattedDate(String formattedDate, String dateFormat) throws ParseException {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(buildDateByParseFormattedDate(formattedDate, dateFormat));
        return calendar;
    }
}
