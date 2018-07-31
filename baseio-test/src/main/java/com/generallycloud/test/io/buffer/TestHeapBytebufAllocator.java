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
import com.generallycloud.baseio.buffer.ByteBufAllocator;
import com.generallycloud.baseio.buffer.PooledByteBufAllocatorGroup;
import com.generallycloud.baseio.buffer.UnpooledByteBufAllocator;
import com.generallycloud.baseio.component.NioEventLoopGroup;

public class TestHeapBytebufAllocator {

    public static void main(String[] args) throws Exception {

        testRead();

    }

    static void test() throws Exception {

        NioEventLoopGroup group = new NioEventLoopGroup();
        group.setMemoryPoolCapacity(10);
        group.setMemoryPoolUnit(1);
        PooledByteBufAllocatorGroup allocator = new PooledByteBufAllocatorGroup(group);

        allocator.start();

        ByteBufAllocator allocator2 = allocator.getNext();

        ByteBuf buf = allocator2.allocate(15);

        System.out.println(buf);
    }
    
    static void testRead(){
        testRead4();
        testRead5();
        testRead6();
        testRead7();
    }

    static void testRead4(){
        
        ByteBuf src = UnpooledByteBufAllocator.getHeap().allocate(1024);
        src.put("hello;hello;".getBytes());
        src.flip();
        
        ByteBuf dst = UnpooledByteBufAllocator.getHeap().allocate(1024);
        dst.read(src);
        dst.flip();
        
        byte [] res = dst.getBytes();
        
        System.out.println(new String(res));
        
    }
    
    static void testRead5(){
        
        ByteBuf src = UnpooledByteBufAllocator.getHeap().allocate(1024);
        src.put("hello;hello;".getBytes());
        src.flip();
        
        ByteBuf dst = UnpooledByteBufAllocator.getHeap().allocate("hello;".length());
        dst.read(src);
        dst.flip();
        
        byte [] res = dst.getBytes();
        
        System.out.println(new String(res));
        
    }
    
    static void testRead6(){
        
        ByteBuf src = UnpooledByteBufAllocator.getDirect().allocate(1024);
        src.put("hello;hello;".getBytes());
        src.flip();
        
        ByteBuf dst = UnpooledByteBufAllocator.getHeap().allocate(1024);
        dst.read(src);
        dst.flip();
        
        byte [] res = dst.getBytes();
        
        System.out.println(new String(res));
        
    }
    
    static void testRead7(){
        
        ByteBuf src = UnpooledByteBufAllocator.getDirect().allocate(1024);
        src.put("hello;hello;".getBytes());
        src.flip();
        
        ByteBuf dst = UnpooledByteBufAllocator.getHeap().allocate("hello;".length());
        dst.read(src);
        dst.flip();
        
        byte [] res = dst.getBytes();
        
        System.out.println(new String(res));
        
    }
    
    
}
