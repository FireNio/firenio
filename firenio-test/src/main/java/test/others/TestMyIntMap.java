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

import java.util.Random;


import test.backup.IntMap;
import test.test.ITestThread;
import test.test.ITestThreadHandle;

/**
 * @author wangkai
 */
public class TestMyIntMap extends ITestThread {

    static final int            time  = 1024 * 1024 * 16;
    static final int[]          array = new int[time];
    static final IntMap<String> map   = new IntMap<>(1024, 0.75f);
    static final String         NULL  = "";

    public static void main(String[] args) {

        int threads  = 1;
        int execTime = 2;
        ITestThreadHandle.doTest(TestMyIntMap.class, threads, time, execTime);

    }

    @Override
    public void run() {
        for (int i = 0; i < array.length; i++) {
            map.put(array[i], NULL);
        }
        for (int i = 0; i < array.length; i++) {
            map.get(array[i]);
            addCount(1280000);
        }
    }

    @Override
    public void prepare() throws Exception {
        Random r = new Random();
        for (int i = 0; i < array.length; i++) {
            array[i] = r.nextInt(Integer.MAX_VALUE);
        }
    }

    @Override
    public void stop() {

    }

}
