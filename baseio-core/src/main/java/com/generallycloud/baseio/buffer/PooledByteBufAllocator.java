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
    private final int              capacity;
    private BitSet                 frees;
    private final ReentrantLock    lock = new ReentrantLock();
    private int                    mask;
    private PooledByteBufAllocator next;
    private final int              unitMemorySize;
    private byte[]                 heapMemory;
    private ByteBuffer             directMemory;
    private final int              allocatorGroupSize;

    public PooledByteBufAllocator(int capacity, int unit, boolean isDirect,
            int allocatorGroupSize) {
        super(isDirect);
        this.capacity = capacity;
        this.unitMemorySize = unit;
        this.allocatorGroupSize = allocatorGroupSize;
    }

    //FIXME 判断余下的是否足够，否则退出循环
    private PooledByteBuf allocate(int limit, int start, int end, int size) {
        int freeSize = 0;
        int[] blockEnds = this.blockEnds;
        BitSet frees = this.frees;
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
                this.mask = blockEnd;
                return newByteBuf().produce(blockStart, blockEnd, limit);
            }
            start++;
        }
        return null;
    }

    PooledByteBuf newByteBuf() {
        return isDirect() ? new PooledDirectByteBuf(this, directMemory.duplicate())
                : new PooledHeapByteBuf(this, heapMemory);
    }

    @Override
    public ByteBuf allocate(int limit) {
        return allocate(limit, 0);
    }

    protected ByteBuffer getDirectMemory() {
        return directMemory;
    }

    protected byte[] getHeapMemory() {
        return heapMemory;
    }

    private ByteBuf allocate(int limit, int current) {
        if (current == allocatorGroupSize) {
            //FIXME 是否申请java内存
            return UnpooledByteBufAllocator.getHeap().allocate(limit);
        }
        int size = (limit + unitMemorySize - 1) / unitMemorySize;
        ReentrantLock lock = this.lock;
        lock.lock();
        ByteBuf buf = null;
        try {
            int mask = this.mask;
            buf = allocate(limit, mask, capacity, size);
            if (buf != null) {
                return buf;
            } else {
                buf = allocate(limit, 0, mask, size);
            }
        } finally {
            lock.unlock();
        }
        if (buf == null) {
            return next.allocate(limit, current + 1);
        }
        return buf;
    }

    @Override
    protected void doStart() throws Exception {
        this.blockEnds = new int[getCapacity()];
        this.frees = new BitSet(getCapacity());
        this.frees.set(0, getCapacity(), true);
        int cap = capacity * unitMemorySize;
        if (isDirect()) {
            this.directMemory = ByteBuffer.allocateDirect(cap);
        } else {
            byte[] memory = this.heapMemory;
            if (memory != null && memory.length == cap) {
                return;
            }
            this.heapMemory = new byte[cap];
        }
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

    private int usedBuf() {
        int used = 0;
        for (int i = 0; i < getCapacity(); i++) {
            if (!frees.get(i)) {
                used++;
            }
        }
        return used;
    }

    private int maxFree() {
        int free = 0;
        int maxFree = 0;
        for (int i = 0; i < getCapacity(); i++) {
            if (frees.get(i)) {
                free++;
            } else {
                maxFree = Integer.max(maxFree, free);
                i = blockEnds[i] - 1;
                free = 0;
            }
        }
        return Integer.max(maxFree, free);
    }

    private int usedMem() {
        int used = 0;
        for (int i = 0; i < getCapacity(); i++) {
            if (!frees.get(i)) {
                int next = blockEnds[i];
                used += (next - i);
                i = next - 1;
            }
        }
        return used;
    }

    @Override
    public void freeMemory() {
        if (isDirect()) {
            ByteBufUtil.release(directMemory);
        } else {
            //FIXME 这里不free了，如果在次申请的时候大小和这次一致，则不在重新申请
            // this.memory = null;
        }
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
    public void release(ByteBuf buf) {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            frees.set(((PooledByteBuf) buf).getUnitOffset());
        } finally {
            lock.unlock();
        }
    }

    protected void setNext(PooledByteBufAllocator allocator) {
        this.next = allocator;
    }

    @Override
    public synchronized String toString() {
        StringBuilder b = new StringBuilder();
        b.append(getClass().getSimpleName());
        b.append("[memory=");
        b.append(getCapacity());
        b.append(",free=");
        b.append(getCapacity() - usedMem());
        b.append(",mfree=");
        b.append(maxFree());
        b.append(",buf=");
        b.append(usedBuf());
        b.append(",isDirect=");
        b.append(isDirect());
        b.append("]");
        return b.toString();
    }

}
