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
import java.util.BitSet;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author wangkai
 *
 */
public class PooledByteBufAllocator extends AbstractByteBufAllocator {

    private int[]                  blockEnds;
    private final ByteBufFactory   bufFactory;
    private final int              capacity;
    private BitSet                 frees;
    private final ReentrantLock    lock = new ReentrantLock();
    private int                    mask;
    private PooledByteBufAllocator next;
    private final int              unitMemorySize;

    public PooledByteBufAllocator(int capacity, int unitMemorySize, boolean isDirect) {
        super(isDirect);
        this.capacity = capacity;
        this.unitMemorySize = unitMemorySize;
        this.bufFactory = isDirect ? new DirectByteBufFactory() : new HeapByteBufFactory();
    }

    private PooledByteBuf allocate(ByteBufBuilder byteBufNew, int limit) {
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

    //FIXME 判断余下的是否足够，否则退出循环
    private PooledByteBuf allocate(ByteBufBuilder byteBufNew, int limit, int start, int end, int size) {
        int freeSize = 0;
        for (; start < end;) {
            int pos = start;
            if (!frees.get(pos)) {
                start = blockEnds[pos];
                freeSize = 0;
                continue;
            }
            if (++freeSize == size) {
                int blockEnd = pos + 1;
                int blockStart = blockEnd - size;
                frees.set(blockStart, false);
                blockEnds[blockStart] = blockEnd;
                mask = blockEnd;
                return byteBufNew.newByteBuf(this).produce(blockStart, blockEnd, limit);
            }
            start++;
        }
        return null;
    }

    @Override
    public ByteBuf allocate(int limit) {
        ByteBuf buf = allocate(bufFactory, limit);
        if (buf == null) {
            return next.allocate(limit, this, bufFactory);
        }
        return buf;
    }

    private ByteBuf allocate(int limit, PooledByteBufAllocator allocator, ByteBufFactory factory) {
        if (allocator == this) {
            //FIXME 是否申请java内存
            return UnpooledByteBufAllocator.getHeap().allocate(limit);
        }
        ByteBuf buf = allocate(factory, limit);
        if (buf == null) {
            return next.allocate(limit, allocator, factory);
        }
        return buf;
    }

    @Override
    protected void doStart() throws Exception {
        this.bufFactory.initializeMemory(capacity * unitMemorySize);
        this.blockEnds = new int[getCapacity()];
        this.frees = new BitSet(getCapacity());
        this.frees.set(0, getCapacity(), true);
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

    //FIXME ..not correct
    private int fillBusy() {
        int free = 0;
        for (int i = 0; i < getCapacity(); i++) {
            if (frees.get(i)) {
                free++;
            }
        }
        return free;
    }

    @Override
    public void freeMemory() {
        bufFactory.freeMemory();
    }

    @Override
    public final int getCapacity() {
        return capacity;
    }

    protected PooledByteBufAllocator getNext() {
        return next;
    }

    @Override
    public final int getUnitMemorySize() {
        return unitMemorySize;
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
            release(buf);
            return buf.newByteBuf(this).produce(newBuf);
        }
        release(buf);
        PooledByteBuf newBuf = allocate(buf, limit);
        if (newBuf == null) {
            throw new BufferException("reallocate failed");
        }
        return newBuf;
    }

    @Override
    public void release(ByteBuf buf) {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            frees.set(((PooledByteBuf) buf).getBeginUnit());
        } finally {
            lock.unlock();
        }
    }

    protected void setNext(PooledByteBufAllocator allocator) {
        this.next = allocator;
    }

    @Override
    public synchronized String toString() {
        int free = fillBusy();
        StringBuilder b = new StringBuilder();
        b.append(this.getClass().getSimpleName());
        b.append("[free=");
        b.append(free);
        b.append(",memory=");
        b.append(getCapacity());
        b.append(",isDirect=");
        b.append(isDirect());
        b.append("]");
        return b.toString();
    }

    interface ByteBufFactory extends ByteBufBuilder {

        void freeMemory();

        void initializeMemory(int capacity);
    }

    final class DirectByteBufFactory implements ByteBufFactory {

        private ByteBuffer memory = null;

        @Override
        public void initializeMemory(int capacity) {
            this.memory = ByteBuffer.allocateDirect(capacity);
        }

        @Override
        public PooledByteBuf newByteBuf(ByteBufAllocator allocator) {
            return new PooledDirectByteBuf(allocator, memory.duplicate());
        }

        @Override
        @SuppressWarnings("restriction")
        public void freeMemory() {
            if (((sun.nio.ch.DirectBuffer) memory).cleaner() != null) {
                ((sun.nio.ch.DirectBuffer) memory).cleaner().clean();
            }
        }

    }

    final class HeapByteBufFactory implements ByteBufFactory {

        private byte[] memory = null;

        @Override
        public void freeMemory() {
            //FIXME 这里不free了，如果在次申请的时候大小和这次一致，则不在重新申请
            // this.memory = null;
        }

        @Override
        public void initializeMemory(int capacity) {
            if (memory != null && memory.length == capacity) {
                return;
            }
            this.memory = new byte[capacity];
        }

        public byte[] getMemory() {
            return memory;
        }

        @Override
        public PooledByteBuf newByteBuf(ByteBufAllocator allocator) {
            return new PooledHeapByteBuf(allocator, memory);
        }

    }

}
