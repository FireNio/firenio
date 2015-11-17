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
import java.util.Arrays;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import com.firenio.baseio.Develop;
import com.firenio.baseio.Options;
import com.firenio.baseio.collection.LinkedBQStack;
import com.firenio.baseio.collection.Stack;
import com.firenio.baseio.common.DateUtil;
import com.firenio.baseio.common.Unsafe;
import com.firenio.baseio.common.Util;

/**
 * @author wangkai
 *
 */
public final class PooledByteBufAllocator extends ByteBufAllocator {

    public static final Map<ByteBuf, BufDebug> BUF_DEBUGS       = new LinkedHashMap<>();
    static final int                           BYTEBUF_BUFFER   = 1024 * 8;
    static final boolean                       BYTEBUF_RECYCLE  = Options.isBufRecycle();
    public static final ByteBufException       EXPANSION_FAILED = EXPANSION_FAILED();

    private long                               address;
    private final int[]                        blockEnds;
    private final Stack<ByteBuf>               bufBuffer;
    private final int                          capacity;
    private ByteBuffer                         directMemory;
    private final BitSet                       frees;
    private final ByteBufAllocatorGroup        group;
    private final int                          groupSize;
    private byte[]                             heapMemory;
    private final boolean                      isDirect;
    private final ReentrantLock                lock             = new ReentrantLock();
    private int                                mark;
    private final int                          nextIndex;
    private final int                          unit;

    public PooledByteBufAllocator(ByteBufAllocatorGroup group, int index) {
        this.group = group;
        this.unit = group.getUnit();
        this.isDirect = group.isDirect();
        this.capacity = group.getCapacity();
        this.groupSize = group.getGroupSize();
        this.frees = new BitSet(getCapacity());
        this.blockEnds = new int[getCapacity()];
        if (BYTEBUF_RECYCLE) {
            bufBuffer = new LinkedBQStack<>(BYTEBUF_BUFFER);
        } else {
            bufBuffer = null;
        }
        int nextIndex = index + 1;
        if (nextIndex == groupSize) {
            nextIndex = 0;
        }
        this.nextIndex = nextIndex;
    }

    @Override
    public ByteBuf allocate() {
        return allocate(unit);
    }

    @Override
    public ByteBuf allocate(int limit) {
        if (Develop.DEBUG) {
            ByteBuf buf = allocate(limit, 0);
            if (buf instanceof ByteBuf) {
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
        if (current == groupSize) {
            // FIXME 是否申请java内存
            return ByteBuf.heap(limit);
        }
        int size = (limit + unit - 1) / unit;
        int blockStart;
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int mark = this.mark;
            blockStart = allocate(mark, capacity, size);
            if (blockStart == -1) {
                blockStart = allocate(0, mark, size);
            }
        } finally {
            lock.unlock();
        }
        if (blockStart == -1) {
            return getNext().allocate(limit, current + 1);
        }
        return newByteBuf().produce(blockStart, blockEnds[blockStart]);
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
                this.mark = blockEnd;
                return blockStart;
                //                return newByteBuf().produce(blockStart, blockEnd, limit);
            }
            start++;
        }
        return -1;
    }

    @Override
    protected void doStart() throws Exception {
        Arrays.fill(blockEnds, 0);
        this.frees.set(0, getCapacity(), true);
        int cap = capacity * unit;
        if (isDirect()) {
            this.directMemory = ByteBuffer.allocateDirect(cap);
            this.address = Unsafe.addressOffset(directMemory);
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
    protected void expansion(ByteBuf buf, int cap) {
        if (cap > buf.capacity()) {
            ReentrantLock lock = this.lock;
            lock.lock();
            try {
                int size = (cap + unit - 1) / unit;
                int blockStart = buf.unitOffset();
                int blockEnd = blockEnds[blockStart];
                int end = blockStart + size;
                int i = blockEnd;
                for (; i < end;) {
                    if (!frees.get(i)) {
                        break;
                    }
                    i++;
                }
                final int mark = this.mark;
                if (i == end) {
                    if (mark < end) {
                        this.mark = end;
                    }
                    blockEnds[blockStart] = end;
                    buf.capacity((end - buf.unitOffset()) * unit);
                    buf.limit(buf.capacity());
                } else {
                    frees.set(blockStart);
                    int pos = allocate(mark, capacity, size);
                    if (pos == -1) {
                        pos = allocate(0, mark, size);
                        if (pos == -1) {
                            throw EXPANSION_FAILED;
                        }
                    }
                    int oldOffset = buf.offset();
                    int oldPos = buf.absPos();
                    int copy = oldPos - oldOffset;
                    buf.produce(pos, blockEnds[pos]);
                    if (isDirect) {
                        Unsafe.copyMemory(address + oldOffset, address + buf.offset(), copy);
                    } else {
                        System.arraycopy(heapMemory, oldOffset, heapMemory, buf.offset(), copy);
                    }
                    buf.position(copy);
                }
            } finally {
                lock.unlock();
            }
        } else {
            buf.limit(cap);
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
        return group.getAllocator(nextIndex);
    }

    public PoolState getState() {
        PoolState state = new PoolState();
        state.buf = usedBuf();
        state.free = getCapacity() - usedMem();
        state.memory = getCapacity();
        state.mfree = maxFree();
        return state;
    }

    @Override
    public final int getUnit() {
        return unit;
    }

    public ByteBuf getUsedBuf(int skip) {
        int skiped = 0;
        for (int i = 0; i < getCapacity(); i++) {
            if (!frees.get(i)) {
                skiped++;
                if (skiped > skip) {
                    return newByteBuf().produce(i, blockEnds[i]);
                }
            }
        }
        return null;
    }

    public boolean isDirect() {
        return isDirect;
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

    ByteBuf newByteBuf() {
        if (BYTEBUF_RECYCLE) {
            ByteBuf buf = bufBuffer.pop();
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
        ByteBuf b = (ByteBuf) buf;
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            frees.set(b.unitOffset());
        } finally {
            lock.unlock();
        }
        if (BYTEBUF_RECYCLE) {
            bufBuffer.push(b);
        }
        if (Develop.DEBUG) {
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

    @Override
    public synchronized String toString() {
        PoolState s = getState();
        StringBuilder b = new StringBuilder();
        b.append(getClass().getSimpleName());
        b.append("[memory=");
        b.append(s.memory);
        b.append(",free=");
        b.append(s.free);
        b.append(",mfree=");
        b.append(s.mfree);
        b.append(",buf=");
        b.append(s.buf);
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

    /**
     * for debug 
     */
    public class PoolState {
        public int buf;
        public int free;
        public int memory;
        public int mfree;
    }

    static ByteBufException EXPANSION_FAILED() {
        return Util.unknownStackTrace(new ByteBufException(), PooledByteBufAllocator.class,
                "expansion");
    }

}
