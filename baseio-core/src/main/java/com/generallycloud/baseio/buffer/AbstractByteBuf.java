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
package com.generallycloud.baseio.buffer;

import java.nio.ByteBuffer;

public abstract class AbstractByteBuf implements ByteBuf {

    protected ByteBufAllocator allocator;
    protected int              offset;
    protected int              capacity;
    protected boolean          released;
    protected int              referenceCount = 0;

    protected AbstractByteBuf(ByteBufAllocator allocator) {
        this.allocator = allocator;
    }

    @Override
    public int forEachByte(ByteProcessor processor) {
        return forEachByte(position(), limit(), processor);
    }

    @Override
    public int forEachByteDesc(ByteProcessor processor) {
        return forEachByteDesc(position(), limit(), processor);
    }

    @Override
    public void get(byte[] dst) {
        get(dst, 0, dst.length);
    }

    @Override
    public byte[] getBytes() {
        byte[] bytes = new byte[remaining()];
        get(bytes);
        return bytes;
    }

    protected int ix(int index) {
        return offset + index;
    }

    @Override
    public int capacity() {
        return capacity;
    }

    @Override
    public int offset() {
        return offset;
    }

    protected void offset(int offset) {
        this.offset = offset;
    }

    @Override
    public void put(byte[] src) {
        put(src, 0, src.length);
    }

    @Override
    public int read(ByteBuffer src) {
        int srcRemaining = src.remaining();
        if (srcRemaining == 0) {
            return 0;
        }
        int remaining = this.remaining();
        if (remaining == 0) {
            return 0;
        }
        return read0(src, srcRemaining, remaining);
    }

    protected abstract int read0(ByteBuffer src, int srcRemaining, int remaining);

    @Override
    public int read(ByteBuf src) {
        int srcRemaining = src.remaining();
        if (srcRemaining == 0) {
            return 0;
        }
        int remaining = this.remaining();
        if (remaining == 0) {
            return 0;
        }
        return read0(src, srcRemaining, remaining);
    }

    protected abstract int read0(ByteBuf src, int srcRemaining, int remaining);

    @Override
    public ByteBuf skipBytes(int length) {
        return position(position() + length);
    }

    @Override
    public ByteBuf reallocate(int limit) {
        return reallocate(limit, false);
    }

    @Override
    public ByteBuf reallocate(int limit, boolean copyOld) {
        return allocator.reallocate(this, limit, copyOld);
    }

    @Override
    public ByteBuf reallocate(int limit, int maxLimit, boolean copyOld) {
        if (limit < 1) {
            throw new BufferException("illegal limit:" + limit);
        }
        if (limit > maxLimit) {
            throw new BufferException("limit:" + limit + ",maxLimit:" + maxLimit);
        }
        return reallocate(limit, copyOld);
    }

    @Override
    public ByteBuf reallocate(int limit, int maxLimit) {
        return reallocate(limit, maxLimit, false);
    }

    @Override
    public void release() {
        synchronized (this) {
            if (released) {
                return;
            }
            if (--referenceCount != 0) {
                return;
            }
            released = true;
            doRelease();
        }
    }

    protected abstract void doRelease();

    @Override
    public boolean isReleased() {
        return released;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(this.getClass().getName());
        b.append("[pos=");
        b.append(position());
        b.append(",lim=");
        b.append(limit());
        b.append(",cap=");
        b.append(capacity());
        b.append(",remaining=");
        b.append(remaining());
        b.append(",offset=");
        b.append(offset);
        b.append("]");
        return b.toString();
    }

}
