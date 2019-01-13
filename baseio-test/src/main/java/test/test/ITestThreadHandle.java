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
package test.test;

import java.math.BigDecimal;

import com.firenio.baseio.common.Util;
import com.firenio.baseio.log.Logger;
import com.firenio.baseio.log.LoggerFactory;

public class ITestThreadHandle {

    private static Logger       logger    = LoggerFactory.getLogger(ITestThreadHandle.class);
    private static long         numOftime;
    private static long         startTime = System.currentTimeMillis();
    private static long         sum;
    public static ITestThread[] ts;

    public static void doTest(Class<? extends ITestThread> clazz, int threads, int time) {
        doTest(clazz, threads, time, 1);
    }

    public static void doTest(Class<? extends ITestThread> clazz, int threads, int time,
            int execTime) {
        for (int i = 0; i < execTime; i++) {
            doTest0(clazz, threads, time / threads);
            Util.sleep(2000);
        }
        logger.info("cost time:{}", (System.currentTimeMillis() - startTime));
        logger.info("################## Test end ####################");
    }

    private static void doTest0(Class<? extends ITestThread> clazz, int threads, int time) {
        numOftime++;
        logger.info("################## Test start ####################");
        int allTime = time * threads;
        ts = new ITestThread[threads];
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
        for (int i = 0; i < ts.length; i++) {
            ts[i].stop();
        }
        double ops = new BigDecimal(allTime * 1000L)
                .divide(new BigDecimal(spend), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
        double avg;
        if (numOftime > 4) {
            sum += ops;
            avg = sum / (numOftime - 4);
        } else {
            avg = ops;
        }
        logger.info("## Execute Time(all):" + allTime);
        logger.info("## Execute Time:" + numOftime);
        logger.info("## OP/S:" + ops);
        logger.info("## OP/S(avg):" + avg);
        logger.info("## Expend Time:" + spend);
    }
}
