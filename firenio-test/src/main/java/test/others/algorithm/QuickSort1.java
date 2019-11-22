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
 * @author wangkai
 */
public class QuickSort1 {

    public static void main(String[] args) {

        TestSort.test(QuickSort1::sort, new int[]{9, 17, 19, 8, 3, 15, 17, 14, 14, 5});
        //        TestSort.test(QuickSort1::sort, new int[]{4, 3, 5});
        TestSort.test_load(QuickSort1::sort, 1024 * 32, 1024);

    }

    private static int p_sort(int[] array, int low, int high) {
        int mid = low + (high - low) / 2;
        int key = (array[low] + array[mid] + array[high]) / 3;
        for (; low < high; ) {
            while (array[high] >= key && high > low) {
                high--;
            }
            while (array[low] <= key && high > low) {
                low++;
            }
            if (low < high) {
                swap(array, low, high);
            } else {
                break;
            }
        }
        return high;
    }

    public static void sort(int[] array) {
        sort(array, 0, array.length);
    }

    public static void sort(int[] array, int low, int high) {
        if (high - low < 8) {
            InsertSort.sort(array, low, high);
        } else {
            int index = p_sort(array, low, high - 1) + 1;
            sort(array, low, index);
            sort(array, index, high);
        }
    }

    private static void swap(int[] array, int a, int b) {
        int temp = array[a];
        array[a] = array[b];
        array[b] = temp;
    }

}
