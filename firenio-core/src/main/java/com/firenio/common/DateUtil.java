/*
 * Copyright 2015 The FireNio Project
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
package com.firenio.common;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtil {

    private static final ThreadLocal<DateUtil> dateUtils       = new ThreadLocal<>();
    private static final String[]              MONTHS          = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    private static final byte[][]              MONTHS_BYTES    = new byte[][]{"Jan".getBytes(), "Feb".getBytes(), "Mar".getBytes(), "Apr".getBytes(), "May".getBytes(), "Jun".getBytes(), "Jul".getBytes(), "Aug".getBytes(), "Sep".getBytes(), "Oct".getBytes(), "Nov".getBytes(), "Dec".getBytes()};
    private static final byte[]                NS              = new byte[10];
    private static final TimeZone              TZ              = TimeZone.getDefault();
    private static final byte                  TZ_0;
    private static final byte                  TZ_1;
    private static final byte                  TZ_2;
    private static final String                TZ_NAME;
    private static final String[]              WEEK_DAYS       = new String[]{"", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    private static final byte[][]              WEEK_DAYS_BYTES = new byte[][]{"".getBytes(), "Sun".getBytes(), "Mon".getBytes(), "Tue".getBytes(), "Wed".getBytes(), "Thu".getBytes(), "Fri".getBytes(), "Sat".getBytes()};

    static {
        boolean daylight = (Calendar.getInstance().get(Calendar.DST_OFFSET) != 0);
        TZ_NAME = TZ.getDisplayName(daylight, TimeZone.SHORT, Locale.getDefault());
        TZ_0 = (byte) TZ_NAME.charAt(0);
        TZ_1 = (byte) TZ_NAME.charAt(1);
        TZ_2 = (byte) TZ_NAME.charAt(2);
        for (int i = 0; i < NS.length; i++) {
            NS[i] = (byte) String.valueOf(i).charAt(0);
        }
    }

    private final DateFormat HH_mm_ss                = new SimpleDateFormat("HH:mm:ss");
    private final DateFormat yyMMdd                  = new SimpleDateFormat("yyMMdd");
    private final DateFormat yyyy_MM_dd              = new SimpleDateFormat("yyyy-MM-dd");
    private final DateFormat yyyy_MM_dd_HH_mm_ss     = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final DateFormat yyyy_MM_dd_HH_mm_ss_SSS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private final DateFormat yyyyMMdd                = new SimpleDateFormat("yyyyMMdd");
    private final DateFormat yyyyMMdd_HH_mm_ss       = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
    private final DateFormat yyyyMMddHHmmss          = new SimpleDateFormat("yyyyMMddHHmmss");
    private       Calendar   calendar                = Calendar.getInstance(TZ);

    public static DateUtil get() {
        DateUtil d = dateUtils.get();
        if (d == null) {
            d = new DateUtil();
            dateUtils.set(d);
        }
        return d;
    }

    public static void main(String[] args) {
        Date d = new Date();
        System.out.println(DateUtil.get().formatYyyy_MM_dd_HH_mm_ss(d));
        String str = get().formatHttp(d.getTime());
        System.out.println(str);
        d = get().parseHttp(str);
        System.out.println(DateUtil.get().formatYyyy_MM_dd_HH_mm_ss(d));
        System.out.println(new String(get().formatHttpBytes()));
        System.out.println(get().formatYyyy_MM_dd_HH_mm_ss(d));
        System.out.println(TZ);
    }

    public String formatHH_mm_ss() {
        return formatHH_mm_ss(new Date());
    }

    public String formatHH_mm_ss(Date date) {
        return HH_mm_ss.format(date);
    }

    public String formatHttp() {
        return formatHttp(System.currentTimeMillis());
    }

    public String formatHttp(long time) {
        calendar.setTimeInMillis(time);
        calendar.setTimeZone(TZ);
        int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
        int month   = calendar.get(Calendar.MONTH);
        int day     = calendar.get(Calendar.DAY_OF_MONTH);
        int year    = calendar.get(Calendar.YEAR);
        int hour    = calendar.get(Calendar.HOUR_OF_DAY);
        int minute  = calendar.get(Calendar.MINUTE);
        int second  = calendar.get(Calendar.SECOND);

        StringBuilder b = new StringBuilder(26);
        b.append(WEEK_DAYS[weekDay]);
        b.append(',');
        b.append(' ');
        if (day < 10) {
            b.append('0');
        }
        b.append(day);
        b.append(' ');
        b.append(MONTHS[month]);
        b.append(' ');
        b.append(year);
        b.append(' ');
        if (hour < 10) {
            b.append('0');
        }
        b.append(hour);
        b.append(':');
        if (minute < 10) {
            b.append('0');
        }
        b.append(minute);
        b.append(':');
        if (second < 10) {
            b.append('0');
        }
        b.append(second);
        b.append(" ");
        b.append(TZ_NAME);

        return b.toString();
    }

    public byte[] formatHttpBytes() {
        byte[] b = new byte[29];
        formatHttpBytes(b, 0, Util.now());
        return b;
    }

    public byte[] formatHttpBytes(long time) {
        byte[] b = new byte[29];
        formatHttpBytes(b, 0, time);
        return b;
    }

    public void formatHttpBytes(byte[] b) {
        formatHttpBytes(b, 0, Util.now());
    }

    //b.len = 29
    public void formatHttpBytes(byte[] b, int off, long time) {
        calendar.setTimeInMillis(time);

        int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
        int month   = calendar.get(Calendar.MONTH);
        int day     = calendar.get(Calendar.DAY_OF_MONTH);
        int year    = calendar.get(Calendar.YEAR);
        int hour    = calendar.get(Calendar.HOUR_OF_DAY);
        int minute  = calendar.get(Calendar.MINUTE);
        int second  = calendar.get(Calendar.SECOND);

        byte[] days   = WEEK_DAYS_BYTES[weekDay];
        byte[] months = MONTHS_BYTES[month];
        b[off + 0] = days[0];
        b[off + 1] = days[1];
        b[off + 2] = days[2];
        b[off + 3] = ',';
        b[off + 4] = ' ';
        b[off + 5] = NS[day / 10];
        b[off + 6] = NS[day % 10];
        b[off + 7] = ' ';
        b[off + 8] = months[0];
        b[off + 9] = months[1];
        b[off + 10] = months[2];
        b[off + 11] = ' ';
        b[off + 12] = NS[year / 1000];
        b[off + 13] = NS[(year / 100) % 10];
        b[off + 14] = NS[(year / 10) % 10];
        b[off + 15] = NS[year % 10];
        b[off + 16] = ' ';
        b[off + 17] = NS[hour / 10];
        b[off + 18] = NS[hour % 10];
        b[off + 19] = ':';
        b[off + 20] = NS[minute / 10];
        b[off + 21] = NS[minute % 10];
        b[off + 22] = ':';
        b[off + 23] = NS[second / 10];
        b[off + 24] = NS[second % 10];
        b[off + 25] = ' ';
        b[off + 26] = TZ_0;
        b[off + 27] = TZ_1;
        b[off + 28] = TZ_2;
    }

    public String formatYyMMdd() {
        return formatYyMMdd(new Date());
    }

    public String formatYyMMdd(Date date) {
        return yyMMdd.format(date);
    }

    //  --------------------------------------------------------------------------------

    public String formatYyyy_MM_dd() {
        return formatYyyy_MM_dd(new Date());
    }

    public String formatYyyy_MM_dd(Date date) {
        return yyyy_MM_dd.format(date);
    }

    public String formatYyyy_MM_dd_HH_mm_ss() {
        return formatYyyy_MM_dd_HH_mm_ss(new Date());
    }

    public String formatYyyy_MM_dd_HH_mm_ss(Date date) {
        return yyyy_MM_dd_HH_mm_ss.format(date);
    }

    public String formatYyyy_MM_dd_HH_mm_ss_SSS() {
        return formatYyyy_MM_dd_HH_mm_ss_SSS(new Date());
    }

    public String formatYyyy_MM_dd_HH_mm_ss_SSS(Date date) {
        return yyyy_MM_dd_HH_mm_ss_SSS.format(date);
    }

    public String formatYyyyMMdd() {
        return formatYyyyMMdd(new Date());
    }

    public String formatYyyyMMdd(Date date) {
        return yyyyMMdd.format(date);
    }

    //  --------------------------------------------------------------------------------

    public String formatYyyyMMdd_HH_mm_ss() {
        return formatYyyyMMdd_HH_mm_ss(new Date());
    }

    public String formatYyyyMMdd_HH_mm_ss(Date date) {
        return yyyyMMdd_HH_mm_ss.format(date);
    }

    public String formatYyyyMMddHHmmss() {
        return formatYyyyMMddHHmmss(new Date());
    }

    public String formatYyyyMMddHHmmss(Date date) {
        return yyyyMMddHHmmss.format(date);
    }

    private int getMonth(String month, int begin, int end) {
        char c1 = month.charAt(begin);
        char c2 = month.charAt(begin + 1);
        char c3 = month.charAt(begin + 2);
        int  c  = (c1 << 16) | (c2 << 8) | c3;
        switch (c) {
            case ('J' << 16) | ('a' << 8) | ('n'):
                return 0;
            case ('F' << 16) | ('e' << 8) | ('b'):
                return 1;
            case ('M' << 16) | ('a' << 8) | ('r'):
                return 2;
            case ('A' << 16) | ('p' << 8) | ('r'):
                return 3;
            case ('M' << 16) | ('a' << 8) | ('y'):
                return 4;
            case ('J' << 16) | ('u' << 8) | ('n'):
                return 5;
            case ('J' << 16) | ('u' << 8) | ('l'):
                return 6;
            case ('A' << 16) | ('u' << 8) | ('g'):
                return 7;
            case ('S' << 16) | ('e' << 8) | ('p'):
                return 8;
            case ('O' << 16) | ('c' << 8) | ('t'):
                return 9;
            case ('N' << 16) | ('o' << 8) | ('v'):
                return 10;
            case ('D' << 16) | ('e' << 8) | ('v'):
                return 11;
            default:
                return -1;
        }
    }

    public Date parseHH_mm_ss(String source) {
        try {
            return HH_mm_ss.parse(source);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public Date parseHttp(String source) {
        int day    = parseInt(source, 5, 7);
        int year   = parseInt(source, 12, 16);
        int hour   = parseInt(source, 17, 19);
        int minute = parseInt(source, 20, 22);
        int second = parseInt(source, 23, 25);
        int month  = getMonth(source, 8, 11);

        Calendar calendar = Calendar.getInstance(TZ);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private int parseInt(String cs, int begin, int end) {
        int sum = 0;
        for (int i = begin; i < end; i++) {
            sum = sum * 10 + (cs.charAt(i) - 48);
        }
        return sum;
    }

    public Date parseYyMMdd(String source) {
        try {
            return yyMMdd.parse(source);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public Date parseYyyy_MM_dd(String source) {
        try {
            return yyyy_MM_dd.parse(source);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public Date parseYyyy_MM_dd_HH_mm_ss(String source) {
        try {
            return yyyy_MM_dd_HH_mm_ss.parse(source);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public Date parseYyyy_MM_dd_HH_mm_ss_SSS(String source) {
        try {
            return yyyy_MM_dd_HH_mm_ss_SSS.parse(source);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public Date parseYyyyMMdd(String source) {
        try {
            return yyyyMMdd.parse(source);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public Date parseYyyyMMdd_HH_mm_ss(String source) {
        try {
            return yyyyMMdd_HH_mm_ss.parse(source);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public Date parseYyyyMMddHHmmss(String source) {
        try {
            return yyyyMMddHHmmss.parse(source);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

}
