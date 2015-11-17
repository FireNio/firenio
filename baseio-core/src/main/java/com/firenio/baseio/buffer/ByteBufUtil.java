/*
 * Copyright 2015 The Baseio Project
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
package com.firenio.baseio.buffer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ByteBufUtil {

    public static UnpooledByteBufAllocator direct() {
        return UnpooledByteBufAllocator.getDirect();
    }

    public static UnpooledByteBufAllocator heap() {
        return UnpooledByteBufAllocator.getHeap();
    }

    public static int read(ByteBuf dst, InputStream src) throws IOException {
        return read(dst, src, dst.capacity());
    }

    public static int read(ByteBuf dst, InputStream src, long limit) throws IOException {
        byte[] array = dst.array();
        if (!dst.hasRemaining()) {
            int read = (int) Math.min(limit, dst.capacity());
            int len = src.read(array, 0, read);
            if (len > 0) {
                dst.position(0);
                dst.limit(len);
            }
            return len;
        }
        int remaining = dst.remaining();
        System.arraycopy(array, dst.position(), array, 0, remaining);
        dst.position(0);
        dst.limit(remaining);
        int read = (int) Math.min(limit, dst.capacity() - remaining);
        int len = src.read(array, remaining, read);
        if (len > 0) {
            dst.limit(remaining + len);
        }
        return len;
    }

    @SuppressWarnings("restriction")
    public static void release(ByteBuffer buffer) {
        if (((sun.nio.ch.DirectBuffer) buffer).cleaner() != null) {
            ((sun.nio.ch.DirectBuffer) buffer).cleaner().clean();
        }
    }

    public static int skip(ByteBuf src, int p, int e, byte v) {
        int i = p;
        for (; i < e; i++) {
            if (src.absByte(i) != v) {
                return i;
            }
        }
        return -1;
    }

}
