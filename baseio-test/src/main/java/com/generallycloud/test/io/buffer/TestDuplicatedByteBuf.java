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
import com.generallycloud.baseio.buffer.UnpooledByteBufAllocator;

/**
 * @author wangkai
 *
 */
public class TestDuplicatedByteBuf {

    public static void main(String[] args) throws Exception {
        
        
//        testDirectP();
//        testHeapP();
        testDirect();
        testHeap();
        
        
    }
    
    static void testDirect() throws Exception{
        ByteBufAllocator a = UnpooledByteBufAllocator.getDirect();
        ByteBuf buf = a.allocate(16);
        
        buf.put("abcdef".getBytes());
        ByteBuf buf2 = buf.duplicate();
        
        System.out.println(new String(buf2.flip().getBytes()));
    }
    
    static void testHeap() throws Exception{
        ByteBufAllocator a = UnpooledByteBufAllocator.getHeap();
        ByteBuf buf = a.allocate(16);
        
        buf.put("abcdef".getBytes());
        ByteBuf buf2 = buf.duplicate();
        
        System.out.println(new String(buf2.flip().getBytes()));
    }
    
    static void testDirectP() throws Exception{
        ByteBufAllocator a = TestAlloc.direct();
        ByteBuf buf = a.allocate(16);
        
        buf.put("abcdef".getBytes());
        ByteBuf buf2 = buf.duplicate();
        
        System.out.println(new String(buf2.flip().getBytes()));
    }
    
    static void testHeapP() throws Exception{
        ByteBufAllocator a = TestAlloc.heap();
        ByteBuf buf = a.allocate(16);
        
        buf.put("abcdef".getBytes());
        ByteBuf buf2 = buf.duplicate();
        
        System.out.println(new String(buf2.flip().getBytes()));
    }
    
    
}
