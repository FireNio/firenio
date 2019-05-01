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
package test.others;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;

import com.firenio.common.Util;

/**
 * @program: firenio
 * @description:
 * @author: wangkai
 * @create: 2019-05-07 15:24
 **/
public class TestYield {

    static final boolean enable_yield  = false;
    static final boolean count_section = false;

    volatile long value = 0;

    static final AtomicLongFieldUpdater<TestYield> value_updater;

    static {
        value_updater = AtomicLongFieldUpdater.newUpdater(TestYield.class, "value");
    }

    public static void main(String[] args) throws Exception {

        test();

        TestYield yield = new TestYield();

        long count = 1 << 24;
        System.out.println("count:" + count);
        Worker w1 = new Worker(yield, count);
        Worker w2 = new Worker(yield, count);
        Util.exec(w1);
        Util.exec(w2);
    }


    static class Worker implements Runnable {

        final TestYield yield;

        final long target;

        Worker(TestYield yield, long target) {
            this.yield = yield;
            this.target = target;
        }

        @Override
        public void run() {
            long old             = Util.now();
            int  success         = 0;
            int  max_success     = 0;
            int  section_success = 0;
            for (; success < target; ) {
                long v = yield.value;
                if (value_updater.compareAndSet(yield, v, v + 1)) {
                    success++;
                    if (count_section) {
                        section_success++;
                    }
                } else {
                    if (enable_yield) {
                        Thread.yield();
                    }
                    if (count_section) {
                        max_success = Math.max(max_success, section_success);
                        section_success = 0;
                    }
                }
            }
            long now = Util.now();
            if (count_section) {
                max_success = Math.max(max_success, section_success);
                System.out.println("max_success:" + max_success);
            }
            System.out.println("time:" + (now - old));
        }
    }


    static void test() {

        long old = Util.now();
        Thread.yield();
        long now = Util.now();

        System.out.println("time:" + (now - old));

    }


}
