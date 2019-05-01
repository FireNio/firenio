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

import java.nio.ByteBuffer;

import com.firenio.codec.http11.HttpMethod;
import com.firenio.common.Unsafe;
import com.firenio.log.LoggerFactory;

/**
 * @author wangkai
 */
public class Test3 {

    //    int v1 = 2;
    //    int v2 = 4;
    //
    HttpMethod m1 = HttpMethod.CONNECT;
    HttpMethod m2 = HttpMethod.CONNECT;
    //
    //    String s1 = "test";
    //    String s2 = "test";

    public static void main(String[] args) {
        LoggerFactory.setEnableSLF4JLogger(false);

        System.out.println(System.getProperty("java.specification.version"));
        System.out.println(System.getProperty("java.version"));

        ByteBuffer b = Unsafe.allocateDirectByteBuffer(100);
        Unsafe.freeByteBuffer(b);
        System.out.println(222);
        


    }

    void test() {
        System.out.println();
    }

}
