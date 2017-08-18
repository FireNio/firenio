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
package com.generallycloud.baseio;

import com.generallycloud.baseio.concurrent.Looper;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

public class LifeCycleUtil {

    private static Logger logger = LoggerFactory.getLogger(LifeCycleUtil.class);

    public static void stop(LifeCycle lifeCycle) {
        if (lifeCycle == null) {
            return;
        }
        try {
            if (lifeCycle.isRunning()) {
                lifeCycle.stop();
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void start(LifeCycle lifeCycle) {

        if (lifeCycle == null) {
            return;
        }

        if (!lifeCycle.isStopped()) {
            return;
        }

        try {
            lifeCycle.start();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void stop(Looper looper) {
        if (looper == null) {
            return;
        }
        try {
            looper.stop();
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
    }

}
