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

import java.math.BigInteger;

import com.firenio.common.Util;

/**
 * @author wangkai
 */
public class FibonacciSeq {

    static long fi(long n) {
        if (n == 0) {
            return 1;
        }
        long n_2 = 0;
        long n_1 = 1;
        long sum = n_2 + n_1;
        for (; n > 0; n--) {
            n_2 = n_1;
            n_1 = sum;
            sum = n_2 + n_1;
        }
        return sum;
    }

    static BigInteger fi1(int n) {
        if (n == 0) {
            return BigInteger.ONE;
        }
        BigInteger n_2 = new BigInteger("0");
        BigInteger n_1 = new BigInteger("1");
        BigInteger sum = n_2.add(n_1);
        for (int i = 1; i < n; i++) {
            n_2 = n_1;
            n_1 = sum;
            sum = n_2.add(n_1);
        }
        return sum;
    }

    public static void main(String[] args) {

        int c = 20;
        for (int i = 0; i < c; i++) {
            System.out.println(fi1(i));
        }
        long start = Util.now_f();
        long res   = fi(1L << 30);
        System.out.println("Time;" + (Util.past(start)));
        System.out.println(res);
    }

}
