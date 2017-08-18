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

import com.generallycloud.baseio.log.DebugUtil;

public class ThreadUtil {

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            DebugUtil.debug(e);
        }
    }

    public static void execute(Runnable runnable) {
        execute(runnable, null);
    }

    public static void execute(Runnable runnable, String name) {
        if (!StringUtil.isNullOrBlank(name)) {
            new Thread(runnable, name).start();
        } else {
            new Thread(runnable).start();
        }
    }

    public static void wait(Object o) {
        try {
            o.wait();
        } catch (InterruptedException e) {}
    }

    public static void wait(Object o, long timeout) {
        try {
            o.wait(timeout);
        } catch (InterruptedException e) {}
    }

}
