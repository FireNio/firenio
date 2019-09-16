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
package test.io;

import java.util.Arrays;

import com.firenio.common.ByteUtil;
import com.firenio.common.Util;

/**
 * @author: wangkai
 **/
public class Test8 {

    public static void main(String[] args) {

        int    count = 10;
        byte[] data  = new byte[1024 * 1024 * 1024];

        //        for (int i = 0; i < data.length; i++) {
        //            data[i] = (byte) i;
        //        }

        Arrays.fill(data, (byte) 1);

        long start = System.nanoTime();

        long res = test_normal(data, count);

        long past = System.nanoTime() - start;

        System.out.println("res: " + res);

        System.out.println("past: " + (past / 1000_1000));

    }


    static long test_normal(byte[] data, int count) {
        long sum = 0;
        for (int i = 0; i < count; i++) {
            for (int j = 0; j < data.length; j++) {
                sum += data[j];
            }
        }
        return sum;
    }


    static long test_exp(byte[] data, int count) {
        long sum1 = 0;
        long sum2 = 0;
        long sum3 = 0;
        long sum4 = 0;
        for (int i = 0; i < count; i++) {
            for (int j = 0; j < data.length; j += 4) {
                sum1 += data[j];
                sum2 += data[j + 1];
                sum3 += data[j + 2];
                sum4 += data[j + 3];
            }
        }
        return sum1 + sum2 + sum3 + sum4;
    }

}
