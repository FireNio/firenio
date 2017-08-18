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
package com.generallycloud.baseio.common;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    private static final DateFormat HH_mm_ss                = new SimpleDateFormat("HH:mm:ss");
    private static final DateFormat yyyy_MM_dd              = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateFormat yyyy_MM_dd_HH_mm_ss     = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");
    private static final DateFormat yyyy_MM_dd_HH_mm_ss_SSS = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss.SSS");
    private static final DateFormat yyyyMMdd_HH_mm_ss       = new SimpleDateFormat(
            "yyyyMMdd HH:mm:ss");
    private static final DateFormat yyyyMMdd                = new SimpleDateFormat("yyyyMMdd");
    private static final DateFormat yyMMdd                  = new SimpleDateFormat("yyMMdd");
    private static final DateFormat yyyyMMddHHmmss          = new SimpleDateFormat(
            "yyyyMMddHHmmss");

    public synchronized static Date parseHH_mm_ss(String source) {
        try {
            return HH_mm_ss.parse(source);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized static Date parseYyyy_MM_dd(String source) {
        try {
            return yyyy_MM_dd.parse(source);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized static Date parseYyyy_MM_dd_HH_mm_ss(String source) {
        try {
            return yyyy_MM_dd_HH_mm_ss.parse(source);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized static Date parseYyyyMMdd_HH_mm_ss(String source) {
        try {
            return yyyyMMdd_HH_mm_ss.parse(source);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized static Date parseYyyyMMdd(String source) {
        try {
            return yyyyMMdd.parse(source);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized static Date parseYyMMdd(String source) {
        try {
            return yyMMdd.parse(source);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized static Date parseYyyyMMddHHmmss(String source) {
        try {
            return yyyyMMddHHmmss.parse(source);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized static Date parseYyyy_MM_dd_HH_mm_ss_SSS(String source) {
        try {
            return yyyy_MM_dd_HH_mm_ss_SSS.parse(source);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    //  --------------------------------------------------------------------------------

    public synchronized static String formatHH_mm_ss(Date date) {
        return HH_mm_ss.format(date);
    }

    public synchronized static String formatYyyy_MM_dd(Date date) {
        return yyyy_MM_dd.format(date);
    }

    public synchronized static String formatYyyy_MM_dd_HH_mm_ss(Date date) {
        return yyyy_MM_dd_HH_mm_ss.format(date);
    }

    public synchronized static String formatYyyyMMdd_HH_mm_ss(Date date) {
        return yyyyMMdd_HH_mm_ss.format(date);
    }

    public synchronized static String formatYyyyMMdd(Date date) {
        return yyyyMMdd.format(date);
    }

    public synchronized static String formatYyMMdd(Date date) {
        return yyMMdd.format(date);
    }

    public synchronized static String formatYyyyMMddHHmmss(Date date) {
        return yyyyMMddHHmmss.format(date);
    }

    public synchronized static String formatYyyy_MM_dd_HH_mm_ss_SSS(Date date) {
        return yyyy_MM_dd_HH_mm_ss_SSS.format(date);
    }

}
