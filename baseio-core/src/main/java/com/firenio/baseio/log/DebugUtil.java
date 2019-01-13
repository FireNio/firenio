/*
 * Copyright 2015 The Baseio Project
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
package com.firenio.baseio.log;

public class DebugUtil {

    private static DebugLogger logger = new DebugLogger(DebugUtil.class);

    public static void debug(Exception e) {
        debug(e.getMessage(), e);
    }

    public static void debug(String message) {
        logger.debug(message);
    }

    public static void debug(String message, Object param) {
        logger.debug(message, param);
    }

    public static void debug(String message, Object param, Object param1) {
        logger.debug(message, param, param1);
    }

    public static void debug(String message, Object[] param) {
        logger.debug(message, param);
    }

    public static void debug(String msg, Throwable e) {
        logger.debug(msg, e);
    }

    public static void error(String message) {
        logger.error(message);
    }

    public static void error(String message, Throwable e) {
        logger.error(message, e);
    }

    public static void error(Throwable e) {
        logger.error(e);
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

    public static DebugLogger getLogger() {
        return logger;
    }

    public static void info(String message) {
        logger.info(message);
    }

    public static void info(String message, Object param) {
        logger.info(message, param);
    }

    public static void info(String message, Object param, Object param1) {
        logger.info(message, param, param1);
    }

    public static void info(String msg, Object... params){
        logger.info(msg, params);
        
    }

}
