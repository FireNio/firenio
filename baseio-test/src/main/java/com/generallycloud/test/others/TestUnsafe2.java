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

import java.nio.ByteBuffer;

import com.generallycloud.baseio.common.UnsafeUtil;

/**
 * @author wangkai
 *
 */
public class TestUnsafe2 {

    public static void main(String[] args) {
        int capacity = 1024 * 1024 * 1;
        ByteBuffer buffer = ByteBuffer.allocateDirect(capacity);
        ByteBuffer heapBuffer = ByteBuffer.allocate(capacity);
        byte[] bb = new byte[capacity];
        long startTime = System.currentTimeMillis();
        int time = 1024 * 16;

        //		testRadByteUnsafeDirectByteBuffer(time, buffer);
        testReadByteDirectByteBuffer(time, buffer);

        System.out.println("Time:" + (System.currentTimeMillis() - startTime));
    }

    static void testRadByteUnsafeDirectByteBuffer(int time, ByteBuffer array) {
        long e = 0;
        long address = UnsafeUtil.addressOffset(array);
        long end = address + array.capacity();
        for (int i = 0; i < time; i++) {
            for (long j = address; j < end; j++) {
                byte b = UnsafeUtil.getByte(j);
                e++;
            }
        }
        System.out.println("e=" + e);
    }

    static void testReadByteDirectByteBuffer(int time, ByteBuffer array) {
        long e = 0;
        int len = array.capacity();
        for (int i = 0; i < time; i++) {
            for (int j = 0; j < len; j++) {
                byte b = array.get(j);
                e++;
            }
        }
        System.out.println("e=" + e);
    }

}
