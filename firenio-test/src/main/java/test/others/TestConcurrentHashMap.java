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

import com.firenio.common.Util;
import test.backup.IntMap;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wangkai
 */
public class TestConcurrentHashMap {

    public static void main(String[] args) {
        int count = 1024 * 1024 * 2;
        //        testConcurrentHashMap(count);
        //        testIntObjectHashMap(count);
        //        testLinkedList(count);
        testPutVal();

    }

    static void testPutVal() {
        Map<Integer, String> map = new ConcurrentHashMap<>(4);
        new Thread(() -> {
            map.remove(1);
        }).start();
        map.put(1, "1");
        map.put(1, "1");
        System.out.println(map);


    }

    static void testConcurrentHashMap(int count) {
        Map<Integer, String> map = new ConcurrentHashMap<>();
        //      map = new HashMap<>();
        for (int i = 0; i < count; i++) {
            map.put(i, String.valueOf(i));
        }
        boolean con       = false;
        long    startTime = Util.now_f();
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
        System.out.println("Time:" + (Util.past(startTime)));
        System.out.println(con);
    }

    static void testIntObjectHashMap(int count) {
        IntMap<String> map = new IntMap<>();
        //      map = new HashMap<>();
        for (int i = 0; i < count; i++) {
            map.put(i, String.valueOf(i));
        }
        boolean con       = false;
        long    startTime = Util.now_f();
        //        for (String v : map.values()) {
        //          if ("1".equals(v)) {
        //              con = true;
        //          }
        //        }
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
        System.out.println("Time:" + (Util.past(startTime)));
        System.out.println(con);
    }

    static void testLinkedList(int count) {
        LinkedList<String> list = new LinkedList<>();
        //      map = new HashMap<>();

        for (int i = 0; i < count; i++) {
            list.add(String.valueOf(i));
        }
        boolean con       = false;
        long    startTime = Util.now_f();
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
        System.out.println("Time:" + (Util.past(startTime)));
        System.out.println(con);
    }

}
