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
package com.generallycloud.test.others.asm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author wangkai
 *
 */
public class Hello {

    // 声明 一个常量
    public static final String FLAG = "我是常量";

    // 普通方法
    public void display() {
        for (int i = 0; i < 8; i++) {
            System.out.println(">>>>>>>>>>" + FLAG);
        }
    }

    // 带有List返回值
    public List<String> testList() {
        List<String> list = new ArrayList<>();
        list.add("Tome");
        list.add("Jack");
        list.add("Lily");
        System.out.println(">>>>>>>>>>testList > list.size = " + list.size());
        return list;
    }

    // 泛型返回值，包含List和Map
    // 两个参数，参数为Map动参
    public List<Map<String, String>> testMapList(boolean val, Map<String, String>... map) {
        List<Map<String, String>> list = new ArrayList<>();
        if (val) {
            for (Map<String, String> m : map) {
                list.add(m);
            }
        }
        System.out.println(">>>>>>>>>>testMapList > list.size = " + list.size());
        return list;
    }

}
