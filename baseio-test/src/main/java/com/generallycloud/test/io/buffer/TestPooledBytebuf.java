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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.ByteBufAllocator;
import com.generallycloud.test.others.TestLinkAndArrayList;

/**
 * @author wangkai
 *
 */
public class TestPooledBytebuf {
    
    
    public static void main(String[] args) throws Exception {
        
        testAlloc();
        
        
    }
    
    
    static void testAlloc() throws Exception{
        ByteBufAllocator a = TestAlloc.heap(64);
        int alloc = 0;
        Random r = new Random();
        List<ByteBuf> bufs = new ArrayList<>();
        for (;;) {
            int pa = r.nextInt((a.getCapacity() - alloc) / 2);
            if (pa == 0) {
                pa = 1;
            }
            ByteBuf buf = a.allocate(pa);
            a.toString();
            bufs.add(buf);
            alloc += buf.limit();
            if (buf.limit() % 3 != 0) {
                buf.release();
                alloc -= buf.limit();
                bufs.remove(buf);
            }
        }
        
//        ByteBuf buf = a.allocate(1);
//        
//        System.out.println(buf);
    }

}
