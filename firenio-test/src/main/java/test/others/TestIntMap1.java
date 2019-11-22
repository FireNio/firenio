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

import io.netty.util.collection.IntObjectHashMap;

import com.firenio.collection.IntMap;
import com.firenio.log.LoggerFactory;

/**
 * @author: wangkai
 **/
public class TestIntMap1 {

    public static void main(String[] args) {
        LoggerFactory.setEnableSLF4JLogger(false);
        //        testNettyMap();
        testMyMap();
    }

    static void testNettyMap() {

        IntObjectHashMap map = new IntObjectHashMap(8);

        map.put(7, "7");
        map.put(15, "15");


        System.out.println(map);

    }

    static void testMyMap() {
        int            test    = 1005;
        String         debug   = "remove 981";
        IntMap<String> map     = new IntMap<>(256);
        String         string  = "put 151\n" + "remove 152";
        String[]       actions = string.split("\n");
        boolean        mark    = false;
        for (int i = 0; i < actions.length; i++) {
            String   action = actions[i];
            String[] ss     = action.split(" ");
            String   sid    = ss[1];
            int      id     = Integer.parseInt(sid);
            if (id == test) {
                mark = true;
                System.out.println(id);
            }
            if (action.equals(debug)) {
                System.out.println(id);
            }
            if ("put".equals(ss[0])) {
                map.put(id, sid);
            } else {
                map.remove(id);
            }
            System.out.println(action);
            if (mark) {
                if (map.get(test) == null) {
                    System.out.println(".........................");
                    return;
                }
            }
        }
        System.out.println(map.get(test));
    }

}
