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

import com.generallycloud.baseio.log.Logger;

public class LoggerUtil {

    private static int    maxNameLength = "PooledByteBufAllocatorManager".length();

    private static String prefix_log    = "[baseio] ";

    private static String getSpace(Logger logger) {
        String name = logger.getName();
        int length = maxNameLength - name.length();
        if (length == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(" ");
        }
        return builder.toString();
    }

    public static void prettyLog(Logger logger, String msg) {
        if (logger == null) {
            return;
        }
        msg = getSpace(logger) + prefix_log + msg;
        logger.info(msg);
    }

    public static void prettyLog(Logger logger, String msg, Object param1) {
        if (logger == null) {
            return;
        }
        msg = getSpace(logger) + prefix_log + msg;
        logger.info(msg, param1);
    }

    public static void prettyLog(Logger logger, String msg, Object param1, Object param2) {
        if (logger == null) {
            return;
        }
        msg = getSpace(logger) + prefix_log + msg;
        logger.info(msg, param1, param2);
    }

    public static void prettyLog(Logger logger, String msg, Object[] param) {
        if (logger == null) {
            return;
        }
        msg = getSpace(logger) + prefix_log + msg;
        logger.info(msg, param);
    }
}
