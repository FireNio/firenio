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
package com.generallycloud.baseio.log;

public class DebugUtil {

    private static DebugLogger printer     = new DebugLogger(DebugUtil.class);

    private static boolean     enableDebug = false;

    public static void debug(String message) {
        printer.debug(message);
    }

    public static void debug(String message, Object param) {
        printer.debug(message, param);
    }

    public static void debug(String message, Object param, Object param1) {
        printer.debug(message, param, param1);
    }

    public static void debug(String message, Object[] param) {
        printer.debug(message, param);
    }

    public static void debug(Throwable e) {
        printer.debug(e);
    }

    public static void error(String message) {
        printer.error(message);
    }

    public static void error(String message, Throwable e) {
        printer.error(message, e);
    }

    public static void error(Throwable e) {
        printer.error(e);
    }

    public static String exception2string(Throwable exception) {
        StackTraceElement[] es = exception.getStackTrace();
        StringBuilder builder = new StringBuilder();
        builder.append(exception.toString());
        for (StackTraceElement e : es) {
            builder.append("\n\tat ");
            builder.append(e.toString());
        }
        return builder.toString();
    }

    public static void info(String message) {
        printer.info(message);
    }

    public static void info(String message, Object param) {
        printer.info(message, param);
    }

    public static void info(String message, Object param, Object param1) {
        printer.info(message, param, param1);
    }

    public static void info(String message, Object[] param) {
        printer.info(message, param);
    }

    public static void setEnableDebug(boolean enable) {
        enableDebug = enable;
    }

    public static boolean isEnableDebug() {
        return enableDebug;
    }

}
