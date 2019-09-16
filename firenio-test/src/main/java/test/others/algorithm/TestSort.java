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
package test.others.algorithm;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Random;

import com.firenio.common.Assert;
import com.firenio.common.Util;
import com.firenio.log.DebugUtil;
import com.firenio.log.LoggerFactory;

/**
 * @author: wangkai
 **/
public class TestSort {

    static {
        LoggerFactory.setEnableSLF4JLogger(false);
    }

    public static int[] random_array() {
        return random_array(10);
    }

    public static int[] random_array(int count) {
        Random random = new Random();
        int[]  array  = new int[count];
        for (int i = 0; i < count; i++) {
            array[i] = random.nextInt(count << 1);
        }
        return array;
    }

    interface TestISort {

        void sort(int[] array);

    }

    public static void test(TestISort sort, int count) {
        test(sort, TestSort.random_array(count));
    }

    public static void test(TestISort sort, int[] array) {
        System.out.println(Util.arrayToString(array));
        int[] copy = Arrays.copyOf(array, array.length);
        sort.sort(array);
        Arrays.sort(copy);
        Assert.expectTrue(array_equals(array, copy));
        System.out.println(Util.arrayToString(array));
    }

    public static void test_load(TestISort sort, int count, int time) {
        int[]   array = random_array(count);
        int[][] sorts = new int[time][];
        for (int i = 0; i < time; i++) {
            sorts[i] = Arrays.copyOf(array, array.length);
        }
        long start = System.nanoTime();
        for (int i = 0; i < time; i++) {
            sort.sort(sorts[i]);
        }
        long past = System.nanoTime() - start;
        DebugUtil.info("sort cost: " + (past / 1000_1000));
    }

    public static boolean array_equals(int[] array1, int[] array2) {
        if (array1.length != array2.length) {
            return false;
        }
        for (int i = 0; i < array1.length; i++) {
            if (array1[i] != array2[i]) {
                return false;
            }
        }
        return true;
    }

}
