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
package com.generallycloud.test.others;

import com.generallycloud.baseio.common.BitSet;
import com.generallycloud.test.test.ITest;
import com.generallycloud.test.test.ITestHandle;

/**
 * @author wangkai
 *
 */
public class TestBitSet {

    public static void main(String[] args) {

        int capacity = 1024 * 1024 * 1024;
        int time = 1;

        //				testMyBitSetCorrect();
        //				testJdkBitSet(capacity,time);
        testMyBitSet(capacity, time);

        System.out.println(32 / 8);
        System.out.println(32 >> 3);

    }

    /**
     * 
     */
    private static void testMyBitSetCorrect() {
        int capacity = 32;
        BitSet set = new BitSet(capacity);
        for (int j = 0; j < capacity; j++) {
            set.set(j);
            System.out.println(set.get(j));
        }
        for (int j = 0; j < capacity; j++) {
            set.clear(j);
            System.out.println(set.get(j));
        }
    }

    static void testMyBitSet(int capacity, int time) {

        final BitSet set = new BitSet(capacity);

        ITestHandle.doTest(new ITest() {

            @Override
            public void test(int i) throws Exception {
                for (int j = 0; j < capacity; j++) {
                    set.set(j);
                }
                for (int j = 0; j < capacity; j++) {
                    set.get(j);
                }
                for (int j = 0; j < capacity; j++) {
                    set.clear(j);
                }
            }
        }, time, "My bitSet");

    }

    static void testJdkBitSet(int capacity, int time) {

        final java.util.BitSet set = new java.util.BitSet(capacity);

        ITestHandle.doTest(new ITest() {

            @Override
            public void test(int i) throws Exception {
                for (int j = 0; j < capacity; j++) {
                    set.set(j);
                }
                for (int j = 0; j < capacity; j++) {
                    set.get(j);
                }
                for (int j = 0; j < capacity; j++) {
                    set.clear(j);
                }
            }
        }, time, "Jdk bitSet");

    }

}
