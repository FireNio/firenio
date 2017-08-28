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
public class BubbleSort {

    public static void main(String[] args) {

        int[] array = new int[30];
        for (int i = 0; i < array.length; i++) {
            array[i] = new Random().nextInt(30);
        }

        CollectionUtil.printArray(array);
        sort(array);
        CollectionUtil.printArray(array);

    }

    public static void sort(int[] array) {
        int len = array.length - 1;
        for (int i = 0; i < len; i++) {
            int jLen = len;
            for (int j = 0; j < jLen; j++) {
                if (array[j] > array[j + 1]) {
                    int temp = array[j];
                    array[j] = array[j + 1];
                    array[j + 1] = temp;
                }
            }
        }
    }

}
