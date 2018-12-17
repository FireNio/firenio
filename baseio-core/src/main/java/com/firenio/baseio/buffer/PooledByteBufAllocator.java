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

import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import com.firenio.baseio.Options;
import com.firenio.baseio.collection.LinkedBQStack;
import com.firenio.baseio.collection.Stack;
import com.firenio.baseio.common.DateUtil;

/**
 * @author wangkai
 *
 */
public class PooledByteBufAllocator extends AbstractByteBufAllocator {

    public static final Map<ByteBuf, BufDebug> BUF_DEBUGS      = new LinkedHashMap<>();
    static final int                           BYTEBUF_BUFFER  = 1024 * 8;
    static final boolean                       BYTEBUF_DEBUG   = Options.isByteBufDebug();
    static final boolean                       BYTEBUF_RECYCLE = Options.isBytebufRecycle();

    private final int                          allocatorGroupSize;
    private int[]                              blockEnds;
    private final Stack<PooledByteBuf>         bufBuffer;
    private final int                          capacity;
    private ByteBuffer                         directMemory;
    private BitSet                             frees;
    private byte[]                             heapMemory;
    private final ReentrantLock                lock            = new ReentrantLock();
    private int                                mask;
    private PooledByteBufAllocator             next;
    private final int                          unitMemorySize;

    public PooledByteBufAllocator(int capacity, int unit, boolean isDirect,
            int allocatorGroupSize) {
        super(isDirect);
        this.capacity = capacity;
        this.unitMemorySize = unit;
        this.allocatorGroupSize = allocatorGroupSize;
        if (BYTEBUF_RECYCLE) {
            bufBuffer = new LinkedBQStack<>(BYTEBUF_BUFFER);
        } else {
            bufBuffer = null;
        }
    }

    @Override
    public ByteBuf allocate(int limit) {
        if (BYTEBUF_DEBUG) {
            ByteBuf buf = allocate(limit, 0);
            if (buf instanceof PooledByteBuf) {
                BufDebug d = new BufDebug();
                d.buf = buf;
                d.e = new Exception(DateUtil.get().formatYyyy_MM_dd_HH_mm_ss_SSS());
                synchronized (BUF_DEBUGS) {
                    BUF_DEBUGS.put(buf, d);
                }
            }
            return buf;
        } else {
            return allocate(limit, 0);
        }
    }

    private ByteBuf allocate(int limit, int current) {
        if (current == allocatorGroupSize) {
            // FIXME 是否申请java内存
            return ByteBufUtil.heap(limit);
        }
        int size = (limit + unitMemorySize - 1) / unitMemorySize;
        int blockStart;
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int mask = this.mask;
            blockStart = allocate(mask, capacity, size);
            if (blockStart == -1) {
                blockStart = allocate(0, mask, size);
            }
        } finally {
            lock.unlock();
        }
        if (blockStart == -1) {
            return next.allocate(limit, current + 1);
        }
        return newByteBuf().produce(blockStart, blockEnds[blockStart], limit);
    }

    // FIXME 判断余下的是否足够，否则退出循环
    private int allocate(int start, int end, int size) {
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
                return blockStart;
                //                return newByteBuf().produce(blockStart, blockEnd, limit);
            }
            start++;
        }
        return -1;
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
    protected void doStop() {
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
        if (isDirect()) {
            ByteBufUtil.release(directMemory);
        } else {
            // FIXME 这里不free了，如果在次申请的时候大小和这次一致，则不在重新申请
            // this.memory = null;
        }
    }

    @Override
    public final int getCapacity() {
        return capacity;
    }

    protected ByteBuffer getDirectMemory() {
        return directMemory;
    }

    protected byte[] getHeapMemory() {
        return heapMemory;
    }

    protected PooledByteBufAllocator getNext() {
        return next;
    }

    @Override
    public final int getUnitMemorySize() {
        return unitMemorySize;
    }

    public ByteBuf getUsedBuf(int skip) {
        int skiped = 0;
        for (int i = 0; i < getCapacity(); i++) {
            if (!frees.get(i)) {
                skiped++;
                if (skiped > skip) {
                    int limit = (blockEnds[i] - i) * getUnitMemorySize();
                    return newByteBuf().produce(i, blockEnds[i], limit);
                }
            }
        }
        return null;
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

    PooledByteBuf newByteBuf() {
        if (BYTEBUF_RECYCLE) {
            PooledByteBuf buf = bufBuffer.pop();
            if (buf == null) {
                return isDirect() ? new PooledDirectByteBuf(this, directMemory.duplicate())
                        : new PooledHeapByteBuf(this, heapMemory);
            }
            return buf;
        } else {
            return isDirect() ? new PooledDirectByteBuf(this, directMemory.duplicate())
                    : new PooledHeapByteBuf(this, heapMemory);
        }
    }

    @Override
    public void release(ByteBuf buf) {
        PooledByteBuf b = (PooledByteBuf) buf;
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            frees.set(b.getUnitOffset());
        } finally {
            lock.unlock();
        }
        if (BYTEBUF_RECYCLE) {
            bufBuffer.push(b);
        }
        if (BYTEBUF_DEBUG) {
            synchronized (BUF_DEBUGS) {
                BufDebug d = BUF_DEBUGS.remove(buf);
                if (d == null) {
                    throw new RuntimeException("null bufDebug");
                }
                d.buf = null;
                d.e = null;
            }
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

    private int usedBuf() {
        int used = 0;
        for (int i = 0; i < getCapacity(); i++) {
            if (!frees.get(i)) {
                used++;
            }
        }
        return used;
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

    public class BufDebug {

        public volatile ByteBuf   buf;
        public volatile Exception e;

    }

}
