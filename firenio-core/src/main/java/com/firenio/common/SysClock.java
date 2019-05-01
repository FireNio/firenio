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

import com.firenio.Options;

/**
 * @program: firenio
 * @description:
 * @author: wangkai
 * @create: 2019-05-10 10:23
 **/
public final class SysClock implements Runnable {

    static final long     STEP = Options.getSysClockStep();
    static final SysClock SYS_CLOCK;
    static final boolean  ENABLE_SYS_CLOCK;

    static {
        long sys_clock_step = Options.getSysClockStep();
        if (sys_clock_step > 0) {
            ENABLE_SYS_CLOCK = true;
            SYS_CLOCK = new SysClock();
            Util.exec(SYS_CLOCK, "sys-clock", true);
        } else {
            SYS_CLOCK = null;
            ENABLE_SYS_CLOCK = false;
        }
    }

    volatile long time;

    @Override
    public void run() {
        for (; ; ) {
            time = now_f();
            Util.sleep(STEP);
        }
    }

    public static long now() {
        if (ENABLE_SYS_CLOCK) {
            return SYS_CLOCK.time;
        } else {
            return System.currentTimeMillis();
        }
    }

    public static long now_f() {
        return System.currentTimeMillis();
    }


}
