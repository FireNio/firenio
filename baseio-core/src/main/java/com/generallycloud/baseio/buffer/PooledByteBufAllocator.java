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

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author wangkai
 *
 */
public abstract class PooledByteBufAllocator extends AbstractByteBufAllocator {

    private final ByteBufFactory   bufFactory;
    private final int              capacity;
    private final ReentrantLock    lock        = new ReentrantLock();
    protected int                  mask;
    private final int              unitMemorySize;
    private PooledByteBufAllocator next;

    public PooledByteBufAllocator(int capacity, int unitMemorySize, int bufRecycleSize,
            boolean isDirect) {
        super(isDirect);
        this.capacity = capacity;
        this.unitMemorySize = unitMemorySize;
        this.bufFactory = isDirect ? new DirectByteBufFactory(bufRecycleSize)
                : new HeapByteBufFactory(bufRecycleSize);
    }

    private PooledByteBuf allocate(ByteBufNew byteBufNew, int limit) {
        int size = (limit + unitMemorySize - 1) / unitMemorySize;
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int mask = this.mask;
            PooledByteBuf buf = allocate(byteBufNew, limit, mask, this.capacity, size);
            if (buf == null) {
                buf = allocate(byteBufNew, limit, 0, mask, size);
            }
            return buf;
        } finally {
            lock.unlock();
        }
    }

    protected final ReentrantLock getLock() {
        return lock;
    }

    abstract PooledByteBuf allocate(ByteBufNew byteBufNew, int limit, int start, int end, int size);

    @Override
    public ByteBuf allocate(int limit) {
        ByteBuf buf = allocate(bufFactory, limit);
        if (buf == null) {
            return next.allocate(limit, this);
        }
        return buf;
    }

    protected ByteBuf allocate(int limit, PooledByteBufAllocator allocator) {
        if (allocator == this) {
            //FIXME 是否申请java内存
            return UnpooledByteBufAllocator.getHeap().allocate(limit);
        }
        ByteBuf buf = allocate(bufFactory, limit);
        if (buf == null) {
            return next.allocate(limit, allocator);
        }
        return buf;
    }

    @Override
    protected void doStart() throws Exception {
        bufFactory.initializeMemory(capacity * unitMemorySize);
    }

    @Override
    protected void doStop() throws Exception {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            freeMemory();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void freeMemory() {
        bufFactory.freeMemory();
    }

    @Override
    public final int getCapacity() {
        return capacity;
    }

    @Override
    public final int getUnitMemorySize() {
        return unitMemorySize;
    }

    protected final ByteBufFactory getBufFactory() {
        return bufFactory;
    }

    @Override
    public ByteBuf reallocate(ByteBuf buf, int limit, boolean copyOld) {
        if (limit <= buf.capacity()) {
            if (copyOld) {
                return buf.limit(limit);
            }
            return buf.position(0).limit(limit);
        }
        if (copyOld) {
            PooledByteBuf newBuf = allocate(bufFactory, limit);
            if (newBuf == null) {
                buf.release();
                throw new BufferException("reallocate failed");
            }
            newBuf.read(buf.flip());
            release((PooledByteBuf) buf, false);
            return buf.newByteBuf(this).produce(newBuf);
        }
        release((PooledByteBuf) buf, true);
        PooledByteBuf newBuf = allocate(buf, limit);
        if (newBuf == null) {
            throw new BufferException("reallocate failed");
        }
        return newBuf;
    }

    protected abstract void release(PooledByteBuf buf, boolean recycle);

    protected void setNext(PooledByteBufAllocator allocator) {
        this.next = allocator;
    }

    public PooledByteBufAllocator getNext() {
        return next;
    }

}
