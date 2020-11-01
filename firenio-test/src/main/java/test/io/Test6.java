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

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wangkai
 */
public class Test6 {

    static Map map = new MyMap();

    public static void main(String[] args) throws Exception {

        map.put("aaa", 111);

        map.forEach((o, o2) -> {
            System.out.println(o);
            System.out.println(o2);
        });



    }

    static class MyMap extends ConcurrentHashMap{

        MyMap(){
            super();
        }

    }

}
