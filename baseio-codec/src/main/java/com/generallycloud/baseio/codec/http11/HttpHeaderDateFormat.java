/*
 * Copyright 2015-2017 GenerallyCloud.com
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
package com.generallycloud.baseio.codec.http11;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.generallycloud.baseio.common.DateUtil;

/**
 * @author wangkai
 *
 */
public class HttpHeaderDateFormat {

    private static final HttpHeaderDateFormat HTTP_HEADER_DATE_FORMAT = new HttpHeaderDateFormat();

    public static HttpHeaderDateFormat getFormat() {
        return HTTP_HEADER_DATE_FORMAT;
    }

    private TimeZone GTM = TimeZone.getTimeZone("GTM");

    public Date parse(String source) {
        int day = Integer.parseInt(source.substring(5, 7));
        int year = Integer.parseInt(source.substring(12, 16));
        int hour = Integer.parseInt(source.substring(17, 19));
        int minute = Integer.parseInt(source.substring(20, 22));
        int second = Integer.parseInt(source.substring(23, 25));
        int month = getMonth(source.substring(8, 11));

        Calendar calendar = Calendar.getInstance(GTM);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static void main(String[] args) {

        String stringDate = "Tue, 11 Apr 2017 05:18:52 GTM";

        System.out.println(stringDate);

        Date d = getFormat().parse(stringDate);

        System.out.println(DateUtil.formatYyyy_MM_dd_HH_mm_ss(d));

        System.out.println(getFormat().format(d.getTime()));
    }

    private int getMonth(String month) {
        switch (month) {
            case "Jan":
                return 0;
            case "Feb":
                return 1;
            case "Mar":
                return 2;
            case "Apr":
                return 3;
            case "May":
                return 4;
            case "Jun":
                return 5;
            case "Jul":
                return 6;
            case "Aug":
                return 7;
            case "Sep":
                return 8;
            case "Oct":
                return 9;
            case "Nov":
                return 10;
            case "Dec":
                return 11;
            default:
                return -1;
        }
    }

    private String[] WEEK_DAYS = new String[] { "", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri",
            "Sat" };

    private String[] MONTHS    = new String[] { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul",
            "Aug", "Sep", "Oct", "Nov", "Dec" };

    public String format(long time) {

        Calendar calendar = Calendar.getInstance(GTM);
        calendar.setTimeInMillis(time);

        int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
        int month = calendar.get(Calendar.MONTH);

        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int year = calendar.get(Calendar.YEAR);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        StringBuilder b = new StringBuilder(25);
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
        b.append(" GTM");

        return b.toString();
    }

}
