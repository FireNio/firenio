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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author wangkai
 *
 */
public class TestLinkedHashMap {

    public static void main(String[] args) {

        Map<String, String> map = new LinkedHashMap<>();

        map.put("ddd", "ddd");
        map.put("fff", "fff");
        map.put("bbb", "bbb");
        map.put("aaa", "aaa");
        map.put("ggg", "ggg");

        System.out.println(map.entrySet().iterator().next().getKey());
        System.out.println(map.keySet().iterator().next());
        System.out.println(map.values().iterator().next());

    }

}
