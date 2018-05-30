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
package com.generallycloud.test.io;

import java.util.ArrayList;
import java.util.List;

import com.generallycloud.baseio.common.Encoding;
import com.generallycloud.baseio.common.FileUtil;
import com.generallycloud.baseio.common.MessageFormatter;

/**
 * @author wangkai
 *
 */
public class Test {

    public static void main(String[] args) throws Exception {
        int max = 8;
        List<String> list = getList(max);
        System.out.println();
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 1024 * 512; i++) {
            //          testForI(list,max);
            //          testForI2(list,max);
            testForeach(list, max);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Time:" + (endTime - startTime));

    }

    static boolean testForeach(List<String> list, int max) {
        for (String i : list) {
            if ("test".equals(i)) {
                return true;
            }
        }
        return false;
    }

    static boolean testForI(List<String> list, int max) {
        for (int i = 0; i < list.size(); i++) {
            if ("test".equals(list.get(i))) {
                return true;
            }
        }
        return false;
    }

    static boolean testForI2(List<String> list, int max) {
        for (int i = 0; i < max; i++) {
            if ("test".equals(list.get(i))) {
                return true;
            }
        }
        return false;
    }

    static List<String> getList(int max) {
        List<String> list = new ArrayList<>(max);
        for (int i = 0; i < max; i++) {
            list.add(String.valueOf(i));
        }
        return list;
    }

    static void test() throws Exception {

        List<String> ls = FileUtil.readLines(FileUtil.readInputStreamByCls("test.txt"),
                Encoding.UTF8);
        boolean req = true;
        for (String l : ls) {
            l = l.trim();
            if (l.length() == 0) {
                req = false;
                continue;
            }
            int idx = l.indexOf(" ");
            String key = l.substring(0, idx);
            String desc = l.substring(idx + 1);
            String key1;
            String key2;
            if (req) {
                key1 = "Req_" + key.replace("-", "_");
                key2 = key.toLowerCase();
            } else {
                key1 = key.replace("-", "_");
                key2 = key;
            }
            String s = MessageFormatter.format("public static final String {} = \"{}\";", key1,
                    key2);
            System.out.println("//" + desc.trim());
            System.out.println(s);

        }
    }

}
