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
package com.firenio.buffer;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import com.firenio.Develop;
import com.firenio.Options;
import com.firenio.collection.ObjectPool;
import com.firenio.common.DateUtil;
import com.firenio.common.Unsafe;
import com.firenio.common.Util;

/**
 * @author wangkai
 */
public final class PooledByteBufAllocator extends ByteBufAllocator {

    public static final Map<ByteBuf, BufDebug>           BUF_DEBUGS            = NEW_BUF_DEBUGS();
    public static final ByteBufException                 EXPANSION_FAILED      = EXPANSION_FAILED();
    static final        int                              ADDRESS_BITS_PER_WORD = 6;
    static final        int                              BUF_POOL_SIZE         = 1024 * 8;
    static final        boolean                          BUF_POOL_ENABLE       = Options.isBufRecycle();
    static final        ThreadLocal<ObjectPool<ByteBuf>> BUF_POOL              = new ThreadLocal<ObjectPool<ByteBuf>>() {

        @Override
        protected ObjectPool<ByteBuf> initialValue() {
            return new ObjectPool<>(Thread.currentThread(), BUF_POOL_SIZE);
        }
    };

    private final int[]            blockEnds;
    private final int              capacity;
    private final long[]           frees;
    private final boolean          isDirect;
    private final int              unit;
    private final ByteBufAllocator unpooled;
    private final ReentrantLock    lock    = new ReentrantLock();
    private       long             address = -1;
    private       ByteBuffer       directMemory;
    private       byte[]           heapMemory;
    private       int              mark;

    public PooledByteBufAllocator(ByteBufAllocatorGroup group) {
        this.unit = group.getUnit();
        this.isDirect = group.isDirect();
        this.capacity = group.getCapacity();
        this.frees = new long[getCapacity() / 8];
        this.blockEnds = new int[getCapacity()];
        this.unpooled = UnpooledByteBufAllocator.get();
    }

    static ByteBufException EXPANSION_FAILED() {
        return Util.unknownStackTrace(new ByteBufException(), PooledByteBufAllocator.class, "expansion");
    }

    @Override
    public ByteBuf allocate() {
        return allocate(unit);
    }

    @Override
    public ByteBuf allocate(int limit) {
        if (limit < 1) {
            return ByteBuf.empty();
        }
        int           size = (limit + unit - 1) / unit;
        int           blockStart;
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (!isRunning()) {
                return unpooled.allocate(limit);
            }
            int mark = this.mark;
            blockStart = allocate(mark, capacity, size);
            if (blockStart == -1) {
                blockStart = allocate(0, mark, size);
            }
        } finally {
            lock.unlock();
        }
        ByteBuf buf;
        if (blockStart == -1) {
            // FIXME 是否申请java内存
            return unpooled.allocate(limit);
        } else {
            buf = newByteBuf().produce(blockStart, size);
            if (Develop.BUF_DEBUG) {
                BufDebug d = new BufDebug();
                d.buf = buf;
                d.e = new Exception(DateUtil.get().formatYyyy_MM_dd_HH_mm_ss_SSS());
                BUF_DEBUGS.put(buf, d);
            }
            return buf;
        }
    }

    // FIXME 判断余下的是否足够，否则退出循环
    private int allocate(int start, int end, int size) {
        int[] blockEnds = this.blockEnds;
        int   pos       = start;
        int   limit     = pos + size;
        if (limit >= end) {
            return -1;
        }
        for (; ; ) {
            if (!isFree(pos)) {
                pos = blockEnds[pos];
                limit = pos + size;
                if (limit >= end) {
                    return -1;
                }
                continue;
            }
            if (++pos == limit) {
                int blockEnd   = pos + 1;
                int blockStart = blockEnd - size;
                clearFree(blockStart);
                blockEnds[blockStart] = blockEnd;
                this.mark = blockEnd;
                return blockStart;
            }
        }
    }

    @Override
    protected void doStart() {
        Arrays.fill(blockEnds, 0);
        Arrays.fill(frees, -1);
        int cap = capacity * unit;
        if (Unsafe.UNSAFE_BUF_AVAILABLE) {
            this.address = Unsafe.allocate(cap);
        } else {
            if (isDirect()) {
                this.directMemory = Unsafe.allocateDirectByteBuffer(cap);
                this.address = Unsafe.address(directMemory);
            } else {
                byte[] memory = this.heapMemory;
                if (memory != null && memory.length == cap) {
                    return;
                }
                this.address = -1;
                this.heapMemory = new byte[cap];
            }
        }
    }

    private static int wordIndex(int bitIndex) {
        return bitIndex >> ADDRESS_BITS_PER_WORD;
    }

    private void setFree(int index) {
        int wordIndex = wordIndex(index);
        frees[wordIndex] |= (1L << index);
    }

    private void clearFree(int index) {
        int wordIndex = wordIndex(index);
        frees[wordIndex] &= ~(1L << index);
    }

    private boolean isFree(int index) {
        int wordIndex = wordIndex(index);
        return ((frees[wordIndex] & (1L << index)) != 0);
    }

    @Override
    protected void doStop() {
        for (; ; ) {
            ReentrantLock lock = this.lock;
            lock.lock();
            try {
                // check all memory(buf) are backed
                if (usedBuf() == 0) {
                    freeMemory();
                    return;
                }
            } finally {
                lock.unlock();
            }
            Util.sleep(8);
        }

    }

    @Override
    protected void expansion(ByteBuf buf, int cap) {
        if (cap > buf.capacity()) {
            ReentrantLock lock = this.lock;
            lock.lock();
            try {
                int size       = (cap + unit - 1) / unit;
                int blockStart = buf.unitOffset();
                int blockEnd   = blockEnds[blockStart];
                int end        = blockStart + size;
                int i          = blockEnd;
                for (; i < end; ) {
                    if (!isFree(i)) {
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
                } else {
                    setFree(blockStart);
                    int pos = allocate(mark, capacity, size);
                    if (pos == -1) {
                        pos = allocate(0, mark, size);
                        if (pos == -1) {
                            throw EXPANSION_FAILED;
                        }
                    }
                    int old_read_index  = buf.readIndex();
                    int old_write_index = buf.writeIndex();
                    int old_offset      = buf.offset();
                    buf.produce(pos, size);
                    if (Unsafe.UNSAFE_BUF_AVAILABLE) {
                        Unsafe.copyMemory(address + old_offset, address + buf.offset(), old_write_index);
                    } else {
                        if (isDirect) {
                            Unsafe.copyMemory(address + old_offset, address + buf.offset(), old_write_index);
                        } else {
                            System.arraycopy(heapMemory, old_offset, heapMemory, buf.offset(), old_write_index);
                        }
                    }
                    buf.readIndex(old_read_index).writeIndex(old_write_index);
                }
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public void freeMemory() {
        if (Unsafe.UNSAFE_BUF_AVAILABLE) {
            Unsafe.free(address);
        } else {
            if (isDirect()) {
                Unsafe.freeByteBuffer(directMemory);
            } else {
                // FIXME 这里不free了，如果在次申请的时候大小和这次一致，则不在重新申请
                // this.memory = null;
            }
        }
    }

    public long getAddress() {
        return address;
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
            if (!isFree(i)) {
                skiped++;
                if (skiped > skip) {
                    return newByteBuf().produce(i, blockEnds[i] - i);
                }
            }
        }
        return null;
    }

    public boolean isDirect() {
        return isDirect;
    }

    private int maxFree() {
        int free    = 0;
        int maxFree = 0;
        for (int i = 0; i < getCapacity(); i++) {
            if (isFree(i)) {
                free++;
            } else {
                maxFree = Math.max(maxFree, free);
                i = blockEnds[i] - 1;
                free = 0;
            }
        }
        return Math.max(maxFree, free);
    }

    private ByteBuf newByteBuf() {
        if (BUF_POOL_ENABLE) {
            ObjectPool<ByteBuf> pool = BUF_POOL.get();
            ByteBuf             buf  = pool.pop();
            if (buf == null) {
                return newByteBuf0(pool);
            }
            return buf;
        } else {
            return newByteBuf0(ObjectPool.BLANK);
        }
    }

    private ByteBuf newByteBuf0(ObjectPool pool) {
        if (Unsafe.UNSAFE_BUF_AVAILABLE) {
            return new PooledUnsafeByteBuf(this, pool, address);
        } else {
            return isDirect() ? new PooledDirectByteBuf(this, pool, directMemory.duplicate()) : new PooledHeapByteBuf(this, pool, heapMemory);
        }
    }

    @Override
    public void release(ByteBuf buf) {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            setFree(buf.unitOffset());
        } finally {
            lock.unlock();
        }
        if (BUF_POOL_ENABLE) {
            buf.recycleObject();
        }
        if (Develop.BUF_DEBUG && buf.isPooled()) {
            BufDebug d = BUF_DEBUGS.remove(buf);
            if (d == null) {
                throw new RuntimeException("null bufDebug");
            }
            d.buf = null;
            d.e = null;
        }
    }

    public String toDebugString() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            PoolState     s = getState();
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
            b.append(",unit=");
            b.append(unit);
            b.append(",isDirect=");
            b.append(isDirect());
            b.append("]");
            return b.toString();
        } finally {
            lock.unlock();
        }
    }

    private int usedBuf() {
        int used = 0;
        for (int i = 0; i < getCapacity(); i++) {
            if (!isFree(i)) {
                used++;
            }
        }
        return used;
    }

    private int usedMem() {
        int used = 0;
        for (int i = 0; i < getCapacity(); i++) {
            if (!isFree(i)) {
                int next = blockEnds[i];
                used += (next - i);
                i = next - 1;
            }
        }
        return used;
    }

    static Map<ByteBuf, BufDebug> NEW_BUF_DEBUGS() {
        return Develop.BUF_DEBUG ? new ConcurrentHashMap<ByteBuf, BufDebug>() : null;
    }

    /**
     * for debug
     */
    public static class BufDebug {

        public volatile ByteBuf   buf;
        public volatile Exception e;

    }

    /**
     * for debug
     */
    public static class PoolState {
        public int buf;
        public int free;
        public int memory;
        public int mfree;
    }

}
