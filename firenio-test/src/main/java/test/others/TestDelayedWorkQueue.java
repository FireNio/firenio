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

import com.firenio.collection.DelayedQueue;
import com.firenio.collection.DelayedQueue.DelayTask;
import com.firenio.common.Util;

/**
 * @author wangkai
 */
public class TestDelayedWorkQueue {

    public static void main(String[] args) {
        DelayedQueue q = new DelayedQueue();
        q.offer(new TestDelayTask(10));
        q.offer(new TestDelayTask(5));
        q.offer(new TestDelayTask(15));
        q.offer(new TestDelayTask(1));
        q.offer(new TestDelayTask(2));
        q.offer(new TestDelayTask(18));
        q.offer(new TestDelayTask(12));
        q.offer(new TestDelayTask(2));

        for (; ; ) {
            DelayTask t = q.poll();
            if (t == null) {
                break;
            }
            t.run();
        }

    }

    static class TestDelayTask extends DelayTask {

        public TestDelayTask(long delay) {
            super(delay - Util.now_f());
        }

        @Override
        public void run() {
            System.out.println(getDelay());
        }

    }

}
