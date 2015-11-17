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

/**
 * @author wangkai
 *
 */
public class Test5 {

    public static void main(String[] args) {

        int all = 15;
        int[] cs = new int[all + 1];

        for (int i = 1; i <= all; i++) {
            int cost = 9999;
            if (i - 1 >= 0) {
                cost = Math.min(cost, cs[i - 1] + 1);
            }
            if (i - 5 >= 0) {
                cost = Math.min(cost, cs[i - 5] + 1);
            }
            if (i - 11 >= 0) {
                cost = Math.min(cost, cs[i - 11] + 1);
            }
            cs[i] = cost;
            System.out.println("cost:" + i + ">" + cost);
        }

        print(all -- + 1);
        System.out.println(all);
    }

    
    
    static void print(int i){
        System.out.println(i);
    }
}
