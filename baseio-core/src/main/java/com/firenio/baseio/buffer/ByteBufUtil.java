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

    public static ByteBuf direct(int cap) {
        return direct().allocate(cap);
    }

    public static UnpooledByteBufAllocator heap() {
        return UnpooledByteBufAllocator.getHeap();
    }

    public static ByteBuf heap(int cap) {
        return heap().allocate(cap);
    }

    public static int read(ByteBuf buf, InputStream inputStream) throws IOException {
        return read(buf, inputStream, buf.capacity());
    }

    public static int read(ByteBuf buf, InputStream inputStream, long limit) throws IOException {
        byte[] array = buf.array();
        if (!buf.hasRemaining()) {
            int read = (int) Math.min(limit, buf.capacity());
            int len = inputStream.read(array, 0, read);
            if (len > 0) {
                buf.position(0);
                buf.limit(len);
            }
            return len;
        }
        int remaining = buf.remaining();
        System.arraycopy(array, buf.position(), array, 0, remaining);
        buf.position(0);
        buf.limit(remaining);
        int read = (int) Math.min(limit, buf.capacity() - remaining);
        int len = inputStream.read(array, remaining, read);
        if (len > 0) {
            buf.limit(remaining + len);
        }
        return len;
    }

    @SuppressWarnings("restriction")
    public static void release(ByteBuffer buffer) {
        if (((sun.nio.ch.DirectBuffer) buffer).cleaner() != null) {
            ((sun.nio.ch.DirectBuffer) buffer).cleaner().clean();
        }
    }

    public static void skip(ByteBuf src,byte v){
        int p = src.absPos();
        int l = src.absLimit();
        int i = p;
        for (; i < l; i++) {
            if (src.absByte(p) != v) {
                src.skip(i - p);
                return;
            }
        }
        src.position(src.limit());
    }
    
    public static ByteBuf wrap(byte[] data) {
        return wrap(data, 0, data.length);
    }
    
    public static ByteBuf wrap(byte[] data, int offset, int length) {
        return heap().wrap(data, offset, length);
    }

    public static ByteBuf wrap(ByteBuffer buffer) {
        return heap().wrap(buffer);
    }

}
