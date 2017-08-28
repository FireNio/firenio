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
package com.generallycloud.test.others.algorithm;

import java.util.Random;

import com.generallycloud.baseio.common.CollectionUtil;

/**
 * @author wangkai
 *
 */
public class QuickSort {

    private static int partition(int[] array, int low, int high) {
        //三数取中
        int mid = low + (high - low) / 2;
        if (array[mid] > array[high]) {
            swap(array, mid, high);
        }
        if (array[low] > array[high]) {
            swap(array, low, high);
        }
        if (array[mid] > array[low]) {
            swap(array, mid, low);
        }
        int key = array[low];

        while (low < high) {
            while (array[high] >= key && high > low) {
                high--;
            }
            array[low] = array[high];
            while (array[low] <= key && high > low) {
                low++;
            }
            array[high] = array[low];
        }
        array[low] = key;
        return high;
    }

    private static void swap(int[] array, int a, int b) {
        int temp = array[a];
        array[a] = array[b];
        array[b] = temp;
    }

    public static void sort(int[] array, int low, int high) {
        if (low >= high) {
            return;
        }
        int index = partition(array, low, high);
        sort(array, low, index - 1);
        sort(array, index + 1, high);
    }

    public static void main(String[] args) {

        int[] array = new int[30];
        for (int i = 0; i < array.length; i++) {
            array[i] = new Random().nextInt(30);
        }

        CollectionUtil.printArray(array);
        sort(array, 0, 29);
        CollectionUtil.printArray(array);

    }

}
