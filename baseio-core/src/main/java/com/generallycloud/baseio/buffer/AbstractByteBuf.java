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
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

public abstract class AbstractByteBuf implements ByteBuf {

    static final AtomicIntegerFieldUpdater<AbstractByteBuf> refCntUpdater;

    static {
        refCntUpdater = AtomicIntegerFieldUpdater.newUpdater(AbstractByteBuf.class,
                "referenceCount");
    }

    protected ByteBufAllocator allocator;
    protected int              offset;
    protected int              capacity;
    protected int              markLimit;
    protected volatile int     referenceCount = 0;

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
        int read = remaining();
        if (read > srcRemaining) {
            read = srcRemaining;
        }
        if (read == 0) {
            return 0;
        }
        return read0(src, read);
    }

    @Override
    public int read(ByteBuffer src, int length) {
        int srcRemaining = src.remaining();
        int remaining = remaining();
        int read = length;
        if (read > srcRemaining) {
            read = srcRemaining;
        }
        if (read > remaining) {
            read = remaining;
        }
        if (read == 0) {
            return 0;
        }
        return read0(src, read);
    }

    protected abstract int read0(ByteBuffer src, int read);

    @Override
    public int read(ByteBuf src) {
        int srcRemaining = src.remaining();
        int read = remaining();
        if (read > srcRemaining) {
            read = srcRemaining;
        }
        if (read == 0) {
            return 0;
        }
        return read0(src, read);
    }

    @Override
    public int read(ByteBuf src, int length) {
        int srcRemaining = src.remaining();
        int remaining = remaining();
        int read = length;
        if (read > srcRemaining) {
            read = srcRemaining;
        }
        if (read > remaining) {
            read = remaining;
        }
        if (read == 0) {
            return 0;
        }
        return read0(src, read);
    }

    protected abstract int read0(ByteBuf src, int read);

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
        int referenceCount = this.referenceCount;
        if (referenceCount < 1) {
            return;
        }
        if (refCntUpdater.compareAndSet(this, referenceCount, referenceCount - 1)) {
            if (referenceCount == 1) {
                allocator.release(this);
            }
            return;
        }
        for (;;) {
            referenceCount = this.referenceCount;
            if (referenceCount < 1) {
                return;
            }
            if (refCntUpdater.compareAndSet(this, referenceCount, referenceCount - 1)) {
                if (referenceCount == 1) {
                    allocator.release(this);
                }
                return;
            }
        }
    }

    protected void addReferenceCount() {
        int referenceCount = this.referenceCount;
        if (refCntUpdater.compareAndSet(this, referenceCount, referenceCount + 1)) {
            return;
        }
        for (;;) {
            referenceCount = this.referenceCount;
            if (refCntUpdater.compareAndSet(this, referenceCount, referenceCount + 1)) {
                break;
            }
        }
    }

    @Override
    public boolean isReleased() {
        return referenceCount < 1;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(getClass().getSimpleName());
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
