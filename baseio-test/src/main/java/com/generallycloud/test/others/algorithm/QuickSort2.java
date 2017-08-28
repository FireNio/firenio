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

/**
 * @author wangkai
 *
 */
public class QuickSort2 {

    private static int partition(ByteArray[] array, int low, int high) {
        //三数取中
        int mid = low + (high - low) / 2;
        if (array[mid].greater(array[high])) {
            swap(array[mid], array[high]);
        }
        if (array[low].greater(array[high])) {
            swap(array[low], array[high]);
        }
        if (array[mid].greater(array[low])) {
            swap(array[mid], array[low]);
        }
        ByteArray key = array[low];

        while (low < high) {
            while (array[high].greaterOrEquals(key) && high > low) {
                high--;
            }
            array[low] = array[high];
            while (array[low].lessOrEquals(key) && high > low) {
                low++;
            }
            array[high] = array[low];
        }
        array[low] = key;//这里low
        return high;
    }

    private static void swap(ByteArray a, ByteArray b) {
        int tmpOff = a.getOff();
        int tmpLen = a.getLength();
        a.setLength(b.getLength());
        a.setOff(b.getOff());
        b.setLength(tmpLen);
        b.setOff(tmpOff);
    }

    public static void sort(ByteArray[] array, int low, int high) {
        if (low >= high) {
            return;
        }
        int index = partition(array, low, high);
        sort(array, low, index - 1);
        sort(array, index + 1, high);
    }

}
