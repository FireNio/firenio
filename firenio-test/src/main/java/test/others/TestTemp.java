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

import com.firenio.common.Assert;
import com.firenio.log.DebugUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author wangkai
 */
public class TestTemp {

    public static void main(String[] args) {

        String t = "tail";

        String head = "head";
        String tail = "tail";
        String p1 = "p";
        String t1 = t;

//        p1 = (t1 != (t1 = tail)) ? t1 : head;

        String temp = null;


        p1 = (print(1,t1) != (t1 = temp = print(3,tail))) ? print(2,t1) : head;

        System.out.println(temp);

        String p2 = "p";
        String t2 = t;


        String t2_temp = t2;
        t2 = tail;

        p2 = t2_temp != tail ? t2 : head;

        Assert.expectTrue(p1 == p2);
        Assert.expectTrue(t1 == t2);



    }

    static String print(int i, String s){
        DebugUtil.info("i: {}, s: {}", i, s);
        return s;
    }

}
