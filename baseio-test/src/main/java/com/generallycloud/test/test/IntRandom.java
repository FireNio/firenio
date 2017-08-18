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
package com.generallycloud.test.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author wangkai
 *
 */
public class IntRandom {

    private List<Integer> list;

    private Random        random = new Random();

    public IntRandom(int max) {
        list = new ArrayList<>(max);
        for (int i = 0; i < max; i++) {
            list.add(i);
        }
    }

    public int nextInt() {
        if (list.size() == 0) {
            return -1;
        }
        return list.remove(random.nextInt(list.size()));
    }

    public static void main(String[] args) {
        int max = 100;
        IntRandom random = new IntRandom(max);

        for (int i = 0; i < max; i++) {
            System.out.print(random.nextInt());
            System.out.print(',');
            if ((i + 1) % 20 == 0) {
                System.out.println();
            }
        }

    }

}
