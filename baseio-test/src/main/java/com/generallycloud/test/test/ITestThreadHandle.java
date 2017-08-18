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
package com.generallycloud.test.test;

import java.math.BigDecimal;

import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

public class ITestThreadHandle {

    private static Logger logger = LoggerFactory.getLogger(ITestThreadHandle.class);

    public static void doTest(Class<? extends ITestThread> clazz, int threads, int time) {

        logger.info("################## Test start ####################");

        int allTime = time * threads;

        ITestThread[] ts = new ITestThread[threads];

        for (int i = 0; i < threads; i++) {

            try {

                ITestThread t = clazz.newInstance();

                t.setTime(time);

                t.prepare();

                ts[i] = t;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        logger.info("## prepare complete");

        long old = System.currentTimeMillis();

        for (int i = 0; i < ts.length; i++) {
            new Thread(ts[i]).start();
        }

        try {
            for (ITestThread t : ts) {
                t.await();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        long spend = (System.currentTimeMillis() - old);

        logger.info("## Execute Time:" + allTime);
        logger.info("## OP/S:" + new BigDecimal(allTime * 1000L).divide(new BigDecimal(spend), 2,
                BigDecimal.ROUND_HALF_UP));
        logger.info("## Expend Time:" + spend);

        for (int i = 0; i < ts.length; i++) {
            ts[i].stop();
        }
    }
}
