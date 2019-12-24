/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.toolkit.time;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期工具
 * Created by hexiaofeng on 15-7-30.
 */
public class DateTime {

    /**
     * 精度到秒的日期格式化
     */
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    /**
     * 精度到天的日期格式化
     */
    public static final String YYYY_MM_DD = "yyyy-MM-dd";

    /**
     * UTC格式
     */
    public static final String UTC_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    protected final Calendar calendar;

    protected DateTime(final Calendar calendar) {
        if (calendar == null) {
            throw new IllegalArgumentException("calendar can not be null");
        }
        this.calendar = calendar;
    }

    /**
     * 构建时间
     *
     * @return 时间
     */
    public static DateTime of() {
        return new DateTime(Calendar.getInstance());
    }

    /**
     * 构建时间
     *
     * @param calendar 日历
     * @return 时间
     */
    public static DateTime of(final Calendar calendar) {
        if (calendar == null) {
            throw new IllegalArgumentException("calendar can not be null");
        }
        return new DateTime(calendar);
    }

    /**
     * 构建时间
     *
     * @param date 时间
     * @return 时间
     */
    public static DateTime of(final Date date) {
        if (date == null) {
            throw new IllegalArgumentException("date can not be null");
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return new DateTime(calendar);
    }

    /**
     * 构建时间
     *
     * @param time 时间
     * @return 时间
     */
    public static DateTime of(final long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return new DateTime(calendar);
    }

    /**
     * 构建时间
     *
     * @param time 时间
     * @return 时间
     */
    public static DateTime of(final String time, final String format) {
        if (format == null || format.isEmpty()) {
            throw new IllegalArgumentException("format can not be empty");
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return of(time, sdf);
    }

    /**
     * 构建时间
     *
     * @param time 时间
     * @return 时间
     */
    public static DateTime of(final String time, final SimpleDateFormat format) {
        if (time == null || time.isEmpty()) {
            throw new IllegalArgumentException("time can not be empty");
        }
        if (format == null) {
            throw new IllegalArgumentException("format can not be null");
        }
        try {
            Date date = format.parse(time);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            return new DateTime(calendar);
        } catch (ParseException e) {
            throw new IllegalArgumentException(String.format("invalid time %s or format %s.", time, format), e);
        }
    }

    /**
     * 增加年
     *
     * @param year 年
     * @return 时间
     */
    public DateTime addYear(final int year) {
        calendar.add(Calendar.YEAR, year);
        return this;
    }

    /**
     * 增加月
     *
     * @param month 月
     * @return 时间
     */
    public DateTime addMonth(final int month) {
        calendar.add(Calendar.MONTH, month);
        return this;
    }

    /**
     * 增加天
     *
     * @param day 天
     * @return 时间
     */
    public DateTime addDay(final int day) {
        calendar.add(Calendar.DAY_OF_YEAR, day);
        return this;
    }

    /**
     * 增加小时
     *
     * @param hour 小时
     * @return 时间
     */
    public DateTime addHour(final int hour) {
        calendar.add(Calendar.HOUR_OF_DAY, hour);
        return this;
    }

    /**
     * 增加分钟
     *
     * @param minute 分钟
     * @return 时间
     */
    public DateTime addMinute(final int minute) {
        calendar.add(Calendar.MINUTE, minute);
        return this;
    }

    /**
     * 增加毫秒
     *
     * @param millisecond 毫秒
     * @return 时间
     */
    public DateTime addMillisecond(final int millisecond) {
        calendar.add(Calendar.MILLISECOND, millisecond);
        return this;
    }


    /**
     * 设置年
     *
     * @param year 年
     * @return 时间
     */
    public DateTime year(final int year) {
        calendar.set(Calendar.YEAR, year);
        return this;
    }

    /**
     * 设置月
     *
     * @param month 月
     * @return 时间
     */
    public DateTime month(final int month) {
        calendar.set(Calendar.MONTH, month);
        return this;
    }

    /**
     * 设置天
     *
     * @param day 天
     * @return 时间
     */
    public DateTime dayOfYear(final int day) {
        calendar.set(Calendar.DAY_OF_YEAR, day);
        return this;
    }

    /**
     * 设置天
     *
     * @param day 天
     * @return 时间
     */
    public DateTime dayOfMonth(final int day) {
        calendar.set(Calendar.DAY_OF_MONTH, day);
        return this;
    }

    /**
     * 设置天
     *
     * @param day 天
     * @return 时间
     */
    public DateTime dayOfWeak(final int day) {
        calendar.set(Calendar.DAY_OF_WEEK, day);
        return this;
    }

    /**
     * 设置小时
     *
     * @param hour 小时
     * @return 时间
     */
    public DateTime hour(final int hour) {
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        return this;
    }

    /**
     * 设置分钟
     *
     * @param minute 分钟
     * @return 时间
     */
    public DateTime minute(final int minute) {
        calendar.set(Calendar.MINUTE, minute);
        return this;
    }

    /**
     * 设置秒
     *
     * @param second 秒
     * @return 时间
     */
    public DateTime second(final int second) {
        calendar.set(Calendar.SECOND, second);
        return this;
    }

    /**
     * 设置秒
     *
     * @param time 时间
     * @return 时间
     */
    public DateTime time(final long time) {
        calendar.setTimeInMillis(time);
        return this;
    }

    /**
     * 获取年
     *
     * @return 年
     */
    public int year() {
        return calendar.get(Calendar.YEAR);
    }

    /**
     * 获取月
     *
     * @return 月
     */
    public int month() {
        return calendar.get(Calendar.MONTH);
    }

    /**
     * 获取每周的第几天
     *
     * @return 每周的第几天
     */
    public int dayOfWeek() {
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    /**
     * 获取每月的第几天
     *
     * @return 每月的第几天
     */
    public int dayOfMonth() {
        if (calendar == null) {
            throw new IllegalArgumentException("calendar can not be null");
        }
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 获取每年的第几天
     *
     * @return 每年的第几天
     */
    public int dayOfYear() {
        return calendar.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * 获取小时
     *
     * @return 小时
     */
    public int hour() {
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    /**
     * 获取分钟
     *
     * @return 分钟
     */
    public int minute() {
        return calendar.get(Calendar.MINUTE);
    }

    /**
     * 获取分钟
     *
     * @return 分钟
     */
    public int second() {
        return calendar.get(Calendar.SECOND);
    }

    /**
     * 获取时间
     *
     * @return 分钟
     */
    public long time() {
        return calendar.getTimeInMillis();
    }

    /**
     * 获取时间
     *
     * @return 分钟
     */
    public Date date() {
        return calendar.getTime();
    }

    /**
     * 设置时间
     *
     * @param date 时间
     * @return 时间
     */
    public DateTime date(Date date) {
        calendar.setTime(date);
        return this;
    }

    /**
     * 获取日历
     *
     * @return 日历
     */
    public Calendar calendar() {
        return calendar;
    }

    /**
     * 到天开始
     *
     * @return 时间
     */
    public DateTime beginOfDay() {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return this;
    }

    /**
     * 到小时开始
     *
     * @return 时间
     */
    public DateTime beginOfHour() {
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return this;
    }

    /**
     * 到分钟开始
     *
     * @return 时间
     */
    public DateTime beginOfMinute() {
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return this;
    }


    /**
     * 到天最大值
     *
     * @return 时间
     */
    public DateTime endOfDay() {
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return this;
    }

    /**
     * 到小时最大值
     *
     * @return 时间
     */
    public DateTime endOfHour() {
        if (calendar == null) {
            throw new IllegalArgumentException("calendar can not be null");
        }
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return this;
    }


    /**
     * 到分钟最大值
     *
     * @return 时间
     */
    public DateTime endOfMinute() {
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return this;
    }

    /**
     * 判断是否在指定日期之后
     *
     * @param target 指定日期
     * @return 是否在指定日期之后
     */
    public boolean after(DateTime target) {
        if (target == null) {
            return false;
        }
        return time() > target.time();
    }

    /**
     * 判断是否大于等于指定日期
     *
     * @param target 指定日期
     * @return 是否在大于等于指定日期
     */
    public boolean ge(DateTime target) {
        if (target == null) {
            return false;
        }
        return time() >= target.time();
    }

    /**
     * 判断是否在指定日期之前
     *
     * @param target 指定日期
     * @return 是否在指定日期之前
     */
    public boolean before(DateTime target) {
        if (target == null) {
            return false;
        }
        return time() < target.time();
    }

    /**
     * 判断是否小于等于指定日期
     *
     * @param target 指定日期
     * @return 是否在小于等于指定日期
     */
    public boolean le(DateTime target) {
        if (target == null) {
            return false;
        }
        return time() <= target.time();
    }

    /**
     * 转换成字符串
     *
     * @param format 格式
     * @return 字符串
     */
    public String toString(final String format) {
        if (format == null || format.isEmpty()) {
            throw new IllegalArgumentException("format can not be empty");
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(calendar.getTime());
    }

    /**
     * 转换成字符串
     *
     * @param format 格式化对象
     * @return 字符串
     */
    public String toString(final SimpleDateFormat format) {
        if (format == null) {
            throw new IllegalArgumentException("format can not be null");
        }
        return format.format(calendar.getTime());
    }

    @Override
    public String toString() {
        return toString(YYYY_MM_DD_HH_MM_SS);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DateTime dateTime = (DateTime) o;

        return calendar.equals(dateTime.calendar);

    }

    @Override
    public int hashCode() {
        return calendar.hashCode();
    }
}
