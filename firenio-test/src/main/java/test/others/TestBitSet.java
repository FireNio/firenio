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

import java.util.BitSet;

/**
 * @author: wangkai
 **/
public class TestBitSet {

    public static void main(String[] args) {

        int    cycle     = 256;
        int    count     = 1024 * 1024;
        long   startTime = System.currentTimeMillis();
//        BitSet set       = new BitSet(count);
        MyBitSet set       = new MyBitSet(count);
        for (int i = 0; i < cycle; i++) {
//            testJdkBitSet(set, count);
                        testMyBitSet(set,count);
        }
        long past = System.currentTimeMillis() - startTime;

        System.out.println("cost: " + past);


    }


    static void testMyBitSet(MyBitSet set, int count) {
        for (int i = 0; i < count; i++) {
            set.setFree(i);
        }
        for (int i = 0; i < count; i++) {
            set.clearFree(i);
        }
    }


    static void testJdkBitSet(BitSet set, int count) {
        for (int i = 0; i < count; i++) {
            set.set(i);
        }
        for (int i = 0; i < count; i++) {
            set.clear(i);
        }
    }

    static class MyBitSet {

        static final int ADDRESS_BITS_PER_WORD = 6;

        private final long[] frees;

        MyBitSet(int cap) {
            frees = new long[cap / 8];
        }

        private static int wordIndex(int bitIndex) {
            return bitIndex >> ADDRESS_BITS_PER_WORD;
        }

        private void setFree(int index) {
            int wordIndex = wordIndex(index);
            frees[wordIndex] |= (1L << index);
        }

        private void clearFree(int index) {
            int wordIndex = wordIndex(index);
            frees[wordIndex] &= ~(1L << index);
        }

        private boolean isFree(int index) {
            int wordIndex = wordIndex(index);
            return ((frees[wordIndex] & (1L << index)) != 0);
        }

    }

}
