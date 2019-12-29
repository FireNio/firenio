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


import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import com.firenio.common.Unsafe;
import com.firenio.common.Util;

/**
 * @author: wangkai
 **/
public class TestNettyByteBuf {

    static volatile ByteBuf buf;

    public static void main(String[] args) {

//        test_exp();

        long res = Unsafe.allocate(1024L * 1024 * 1024 * 4);
        System.out.println(res);

    }

    static void test_exp(){
        ByteBufAllocator a = ByteBufAllocator.DEFAULT;
        ByteBuf buf =  a.buffer(512);
        System.out.println(buf);
        buf.writerIndex(508);
        buf.writeLong(1L);
        System.out.println(buf);
    }

    static void test_release(){
        ByteBufAllocator a = ByteBufAllocator.DEFAULT;
        Util.exec(() ->{
            buf = a.buffer(16);
        });
        Util.exec(() ->{
            buf.release();
        });
        System.out.println(buf);
    }



}
