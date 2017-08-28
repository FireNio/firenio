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

import java.util.ArrayList;
import java.util.List;

import com.generallycloud.test.test.ITest;
import com.generallycloud.test.test.ITestHandle;

/**
 * @author wangkai
 *
 */
public class ZeroOnePackage {

    public static int getValue(int[] values, int[] weights, int pWeight, int[][] table,
            boolean print) {
        int valuesLen = values.length;
        for (int i = 1; i < pWeight; i++) {
            int w = weights[0];
            int[] rows = table[i];
            if (w <= i) {
                rows[1] = values[0];
            }
            for (int j = 1; j < valuesLen; j++) {
                w = weights[j];
                int notPut = rows[j - 1];
                if (w > i) {
                    rows[j] = notPut;
                    continue;
                }
                int put = table[i - w][j - 1] + values[j];
                rows[j] = Math.max(notPut, put);
            }
            printTable(table, print);
        }
        if (print) {
            List<Integer> res = new ArrayList<>();
            int j = pWeight - 1;
            int length = valuesLen - 1;
            for (int i = length; i > 0; --i) {
                if (table[j][i] > table[j][i - 1]) {
                    j = j - weights[i];
                    res.add(i);
                }
            }
            System.out.println("choose index : " + res);
        }
        return table[pWeight - 1][valuesLen - 1];
    }

    private static void printTable(int[][] table, boolean print) {
        if (!print) {
            return;
        }
        System.out.println("==================================");
        for (int i = 0; i < table.length; i++) {
            int[] rows = table[i];
            System.out.print('[');
            for (int j = 0; j < rows.length; j++) {
                System.out.print(rows[j]);
                System.out.print(", ");

            }
            System.out.println("]");
        }
    }

    public static void main(String[] args) {
        long time = 1 * 10000;

        //		test3(time);
        test2(time);
    }

    static void test1() {
        int[] values = new int[] { 0, 6, 4, 5, 3, 6 };
        int[] weights = new int[] { 0, 4, 5, 6, 2, 2 };
        int pWeight = 10;
        int[][] table = new int[pWeight + 1][values.length];

        int max = getValue(values, weights, pWeight + 1, table, true);

        System.out.println(max);
    }

    static void test2(long time) {

        int[] values = new int[] { 0, 509, 838, 924, 650, 604, 793, 564, 651, 697, 649, 747, 787,
                701, 605, 644 };
        int[] weights = new int[] { 0, 509, 838, 924, 650, 604, 793, 564, 651, 697, 649, 747, 787,
                701, 605, 644 };
        int pWeight = 5000;
        int[][] table = new int[pWeight + 1][values.length];

        ITestHandle.doTest(new ITest() {

            @Override
            public void test(int i) throws Exception {
                getValue(values, weights, pWeight + 1, table, false);

            }
        }, time, "My");

    }

    static void test3(long time) {

        int[] values = new int[] { 509, 838, 924, 650, 604, 793, 564, 651, 697, 649, 747, 787, 701,
                605, 644 };
        int[] weights = new int[] { 509, 838, 924, 650, 604, 793, 564, 651, 697, 649, 747, 787, 701,
                605, 644 };
        int pWeight = 5000;
        // Items are in rows and weight at in columns +1 on each side
        int[][] V = new int[weights.length + 1][pWeight + 1];

        ITestHandle.doTest(new ITest() {
            @Override
            public void test(int i) throws Exception {
                knapsack(values, weights, pWeight, V, false);
            }
        }, time, "knapsack");
    }

    public static int knapsack(int val[], int wt[], int W, int[][] V, boolean print) {
        // Get the total number of items.
        // Could be wt.length or val.length. Doesn't matter
        int N = wt.length;

        // Create a matrix.

        // What if the knapsack's capacity is 0 - Set
        // all columns at row 0 to be 0
        for (int col = 0; col <= W; col++) {
            V[0][col] = 0;
        }

        // What if there are no items at home.
        // Fill the first row with 0
        for (int row = 0; row <= N; row++) {
            V[row][0] = 0;
        }

        for (int item = 1; item <= N; item++) {
            // Let's fill the values row by row
            for (int weight = 1; weight <= W; weight++) {
                // Is the current items weight less
                // than or equal to running weight
                if (wt[item - 1] <= weight) {
                    // Given a weight, check if the value of the current
                    // item + value of the item that we could afford
                    // with the remaining weight is greater than the value
                    // without the current item itself
                    V[item][weight] = Math.max(val[item - 1] + V[item - 1][weight - wt[item - 1]],
                            V[item - 1][weight]);
                } else {
                    // If the current item's weight is more than the
                    // running weight, just carry forward the value
                    // without the current item
                    V[item][weight] = V[item - 1][weight];
                }
            }

        }

        // Printing choose
        if (print) {
            int j = W;
            int length = N;
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = length; i > 0; --i) {
                //转态转移的时候记录
                if (V[i][j] > V[i - 1][j]) {
                    stringBuilder.append(i).append("-");
                    j = j - wt[i - 1];
                }
            }
            String result = stringBuilder.toString();
            System.out.println("choose index : " + result.substring(0, result.length() - 1));
        }
        return V[N][W];
    }

}
