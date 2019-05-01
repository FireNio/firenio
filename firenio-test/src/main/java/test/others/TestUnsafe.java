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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.firenio.common.ByteUtil;
import com.firenio.common.Unsafe;
import com.firenio.common.Util;
import com.firenio.log.DebugUtil;

/**
 * @author wangkai
 */
public class TestUnsafe {

    public static void main(String[] args) {
        int        capacity   = 1024 * 1024 * 1;
        ByteBuffer buffer     = Unsafe.allocateDirectByteBuffer(capacity);
        ByteBuffer heapBuffer = ByteBuffer.allocate(capacity);
        byte[]     bb         = new byte[capacity];
        long       startTime  = Util.now_f();
        int        time       = 1024 * 16;

        //        		testPutIntDirectByteBuffer(time, buffer); //5600
        //		testPutIntHeapBuffer(time, heapBuffer); //9700
        //              testPutIntUnsafe(time, buffer); //1700
        //		testPutIntByteArray(time, bb); //7000

        //              testGetIntDirectByteBuffer(time, buffer);
        testGetIntUnsafe(time, buffer);

        //        		testReadByteUnsafe(time, bb); //7000
        //        		testReadByteByteArray(time, bb); //80
        //        		testRadByteUnsafeDirectByteBuffer(time, buffer); //11500
        //        		testReadByteDirectByteBuffer(time, buffer); //80

        //		testWriteByteByteArray(time, bb); //820
        //		testWriteByteUnsafe(time, bb); //11250

        DebugUtil.info("Time:{}", Util.past(startTime));
    }

    static void testGetIntDirectByteBuffer(int time, ByteBuffer buffer) {
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < time; i++) {
            buffer.clear();
            for (; buffer.hasRemaining(); ) {
                buffer.getInt();
            }
        }
    }

    static void testGetIntUnsafe(int time, ByteBuffer buffer) {
        long address = Unsafe.address(buffer);
        long end     = address + buffer.capacity();
        for (int i = 0; i < time; i++) {
            for (long j = address; j < end; ) {
                Unsafe.getInt(address);
                j += 4;
            }
        }
    }

    static void testPutIntByteArray(int time, byte[] array) {
        int len = array.length;
        for (int i = 0; i < time; i++) {
            for (int j = 0; j < len; ) {
                //				MathUtil.int2ByteLE(array, 999809234, j);
                ByteUtil.putInt(array, 999809234, j);
                j += 4;
            }
        }
    }

    static void testPutIntDirectByteBuffer(int time, ByteBuffer buffer) {
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < time; i++) {
            buffer.clear();
            for (; buffer.hasRemaining(); ) {
                buffer.putInt(999809234);
            }
        }
    }

    static void testPutIntHeapBuffer(int time, ByteBuffer buffer) {
        buffer.order(ByteOrder.BIG_ENDIAN);
        for (int i = 0; i < time; i++) {
            buffer.clear();
            for (; buffer.hasRemaining(); ) {
                buffer.putInt(999809234);
            }
        }
    }

    static void testPutIntUnsafe(int time, ByteBuffer buffer) {
        long address = Unsafe.address(buffer);
        long end     = address + buffer.capacity();
        for (int i = 0; i < time; i++) {
            for (long j = address; j < end; ) {
                Unsafe.putInt(address, 999809234);
                j += 4;
            }
        }
    }

    static void testRadByteUnsafeDirectByteBuffer(int time, ByteBuffer array) {
        byte e       = 0;
        long address = Unsafe.address(array);
        long end     = address + array.capacity();
        for (int i = 0; i < time; i++) {
            for (long j = address; j < end; j++) {
                e = Unsafe.getByte(j);
            }
        }
        DebugUtil.info("e={}", e);
    }

    static void testReadByteByteArray(int time, byte[] array) {
        byte e   = 0;
        int  len = array.length;
        for (int i = 0; i < time; i++) {
            for (int j = 0; j < len; j++) {
                e = array[j];
            }
        }
        DebugUtil.info("e={}", e);
    }

    static void testReadByteDirectByteBuffer(int time, ByteBuffer array) {
        byte e   = 0;
        int  len = array.capacity();
        for (int i = 0; i < time; i++) {
            for (int j = 0; j < len; j++) {
                e = array.get(j);
            }
        }
        DebugUtil.info("e={}", e);
    }

    static void testReadByteUnsafe(int time, byte[] array) {
        int  len     = array.length;
        byte e       = 0;
        long address = Unsafe.getArrayBaseOffset();
        long end     = address + len;
        for (int i = 0; i < time; i++) {
            for (long j = address; j < end; j++) {
                e = Unsafe.getByte(array, j);
            }
        }
        DebugUtil.info("e={}", e);
    }

    static void testWriteByteByteArray(int time, byte[] array) {
        int  len = array.length;
        byte b   = 1;
        long e   = 0;
        for (int i = 0; i < time; i++) {
            for (int j = 0; j < len; j++) {
                array[j] = b;
                e++;
            }
        }
        DebugUtil.info("e={}", e);
    }

    static void testWriteByteUnsafe(int time, byte[] array) {
        int  len     = array.length;
        byte b       = 1;
        long e       = 0;
        long address = Unsafe.getArrayBaseOffset();
        long end     = address + len;
        for (int i = 0; i < time; i++) {
            for (long j = address; j < end; j++) {
                Unsafe.putByte(array, j, b);
                e++;
            }
        }
        DebugUtil.info("e={}", e);
    }

}
