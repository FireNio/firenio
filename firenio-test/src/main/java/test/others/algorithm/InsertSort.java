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

/**
 * @author: wangkai
 **/
public class InsertSort {

    public static void main(String[] args) {
        TestSort.test(InsertSort::sort, 10);
        //        TestSort.test_load(InsertSort::sort, 1024 * 32, 16);
        TestSort.test_load(InsertSort::sort, 16, 1024 * 1024 * 8);
    }

    public static void sort(int[] array, int off, int end) {
        if (end - off < 2) {
            return;
        }
        int s0 = array[off + 0];
        int s1 = array[off + 1];
        if (s1 < s0) {
            array[off + 0] = s1;
            array[off + 1] = s0;
        }
        direct_sort(array, off, end);
    }

    public static void sort(int[] array) {
        sort(array, 0, array.length);
    }

    static void move_des(int[] array, int j, int i) {
        for (int k = i; k > j; k--) {
            array[k] = array[k - 1];
        }
    }

    static void direct_sort(int[] array, int off, int end) {
        for (int i = off + 2; i < end; i++) {
            int temp    = array[i];
            int mid_idx = (i + off) >>> 1;
            int j       = temp < array[mid_idx] ? off : mid_idx;
            for (; j < i; j++) {
                if (temp < array[j]) {
                    move_des(array, j, i);
                    array[j] = temp;
                    break;
                }
            }
        }
    }

    static void half_sort(int[] array, int off, int end) {
        for (int i = off + 2; i < end; i++) {
            int temp = array[i];
            int j    = half_find(array, off, i, temp);
            if (j < i) {
                move_des(array, j, i);
                array[j] = temp;
            }
        }
    }

    static int half_find(int[] array, int low, int high, int value) {
        int mid = high >>> 1;
        for (; ; ) {
            int temp = array[mid];
            if (value > temp) {
                low = mid;
            } else if (temp == value) {
                return mid;
            } else {
                high = mid;
            }
            if (high - low == 1) {
                if (value < array[low]) {
                    return low;
                } else {
                    return high;
                }
            }
            mid = (low + high) >>> 1;
        }
    }

    static void move_aes(int[] array, int j, int i) {
        int t1 = array[j];
        int t2 = array[j + 1];
        for (int k = j + 1; k < i; k++) {
            t2 = array[k];
            array[k] = t1;
            t1 = t2;
        }
        array[i] = t1;
    }

}
