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
package com.generallycloud.test.others;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.generallycloud.baseio.collection.IntObjectHashMap;

/**
 * @author wangkai
 *
 */
public class TestConcurrentHashMap {

    public static void main(String[] args) {
        int count = 1024 * 1024 * 2;
        //        testConcurrentHashMap(count);
        //        testIntObjectHashMap(count);
        testLinkedList(count);

    }

    static void testLinkedList(int count) {
        LinkedList<String> list = new LinkedList<>();
        //      map = new HashMap<>();

        for (int i = 0; i < count; i++) {
            list.add(String.valueOf(i));
        }
        boolean con = false;
        long startTime = System.currentTimeMillis();
        for (String v : list) {
            //          if ("1".equals(v)) {
            //              con = true;
            //          }
        }
        //      for(String v : map.values()){
        ////          if ("1".equals(v)) {
        ////              con = true;
        ////          }
        //      }
        //      for(String v : map.values()){
        ////          if ("1".equals(v)) {
        ////              con = true;
        ////          }
        //      }
        System.out.println("Time:" + (System.currentTimeMillis() - startTime));
        System.out.println(con);
    }

    static void testIntObjectHashMap(int count) {
        IntObjectHashMap<String> map = new IntObjectHashMap<>();
        //      map = new HashMap<>();
        for (int i = 0; i < count; i++) {
            map.put(i, String.valueOf(i));
        }
        boolean con = false;
        long startTime = System.currentTimeMillis();
        for (String v : map.values()) {
            //          if ("1".equals(v)) {
            //              con = true;
            //          }
        }
        //      for(String v : map.values()){
        ////          if ("1".equals(v)) {
        ////              con = true;
        ////          }
        //      }
        //      for(String v : map.values()){
        ////          if ("1".equals(v)) {
        ////              con = true;
        ////          }
        //      }
        System.out.println("Time:" + (System.currentTimeMillis() - startTime));
        System.out.println(con);
    }

    static void testConcurrentHashMap(int count) {
        Map<Integer, String> map = new ConcurrentHashMap<>();
        //      map = new HashMap<>();
        for (int i = 0; i < count; i++) {
            map.put(i, String.valueOf(i));
        }
        boolean con = false;
        long startTime = System.currentTimeMillis();
        for (String v : map.values()) {
            //          if ("1".equals(v)) {
            //              con = true;
            //          }
        }
        //      for(String v : map.values()){
        ////          if ("1".equals(v)) {
        ////              con = true;
        ////          }
        //      }
        //      for(String v : map.values()){
        ////          if ("1".equals(v)) {
        ////              con = true;
        ////          }
        //      }
        System.out.println("Time:" + (System.currentTimeMillis() - startTime));
        System.out.println(con);
    }

}
