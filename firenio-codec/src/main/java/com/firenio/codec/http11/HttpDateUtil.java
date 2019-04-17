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
package com.firenio.codec.http11;

import com.firenio.common.DateUtil;
import com.firenio.common.Util;

/**
 * @author wangkai
 */
public class HttpDateUtil {

    private static final HttpDateTimeClock CLOCK = new HttpDateTimeClock();
    private static final ThreadLocal<HttpDateUtil> LOCAL;
    private static       boolean           inited;

    static {

        LOCAL = new ThreadLocal<HttpDateUtil>() {

            protected HttpDateUtil initialValue() {
                return new HttpDateUtil();
            }
        };

    }

    private final byte[] value      = new byte[29];
    private final byte[] line_value = new byte[8 + 29];
    private       long   time;

    public HttpDateUtil() {
        System.arraycopy("\r\nDate: ".getBytes(), 0, line_value, 0, 8);
    }

    public static byte[] getDate() {
        return getLocalHttpDateUtil().value;
    }

    private static HttpDateUtil getLocalHttpDateUtil() {
        HttpDateUtil u   = LOCAL.get();
        long         now = CLOCK.time;
        if (now > u.time) {
            u.time = now + 1000;
            DateUtil.get().formatHttpBytes(u.value, 0, now);
            DateUtil.get().formatHttpBytes(u.line_value, 8, now);
        }
        return u;
    }

    public static byte[] getDateLine() {
        return getLocalHttpDateUtil().line_value;
    }

    public static synchronized void start() {
        if (!inited) {
            Util.exec(CLOCK, "http-date-clock");
        }
    }

    public static synchronized void stop() {
        if (inited) {
            CLOCK.stop();
        }
    }

    private static class HttpDateTimeClock implements Runnable {

        volatile boolean running;

        volatile long time;

        @Override
        public void run() {
            running = true;
            for (; running; ) {
                time = System.currentTimeMillis();
                Util.sleep(1000);
            }
        }

        void stop() {
            running = false;
            Thread.currentThread().interrupt();
        }

    }

}
