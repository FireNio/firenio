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
package com.generallycloud.test.io.buffer;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.UnpooledByteBufAllocator;

public class TestDirectBytebufAllocator {

    public static void main(String[] args) throws Exception {

        testRead();

    }

    static void testRead(){
        testRead0();
        testRead1();
        testRead2();
        testRead3();
    }

    static void testRead0(){
        
        ByteBuf src = UnpooledByteBufAllocator.getDirect().allocate(1024);
        src.put("hello;hello;".getBytes());
        src.flip();
        
        ByteBuf dst = UnpooledByteBufAllocator.getDirect().allocate(1024);
        dst.read(src);
        dst.flip();
        
        byte [] res = dst.getBytes();
        
        System.out.println(new String(res));
        
    }
    
    static void testRead1(){
        
        ByteBuf src = UnpooledByteBufAllocator.getDirect().allocate(1024);
        src.put("hello;hello;".getBytes());
        src.flip();
        
        ByteBuf dst = UnpooledByteBufAllocator.getDirect().allocate("hello;".length());
        dst.read(src);
        dst.flip();
        
        byte [] res = dst.getBytes();
        
        System.out.println(new String(res));
        
    }
    
    static void testRead2(){
        
        ByteBuf src = UnpooledByteBufAllocator.getHeap().allocate(1024);
        src.put("hello;hello;".getBytes());
        src.flip();
        
        ByteBuf dst = UnpooledByteBufAllocator.getDirect().allocate(1024);
        dst.read(src);
        dst.flip();
        
        byte [] res = dst.getBytes();
        
        System.out.println(new String(res));
        
    }
    
    static void testRead3(){
        
        ByteBuf src = UnpooledByteBufAllocator.getHeap().allocate(1024);
        src.put("hello;hello;".getBytes());
        src.flip();
        
        ByteBuf dst = UnpooledByteBufAllocator.getDirect().allocate("hello;".length());
        dst.read(src);
        dst.flip();
        
        byte [] res = dst.getBytes();
        
        System.out.println(new String(res));
        
    }
    
    
}
