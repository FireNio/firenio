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
import com.firenio.common.DateUtil;
import com.firenio.common.Unsafe;
import com.firenio.common.Util;

/**
 * @author wangkai
 */
public final class PooledByteBufAllocator extends ByteBufAllocator {

    public static final Map<ByteBuf, BufDebug> BUF_DEBUGS            = NEW_BUF_DEBUGS();
    public static final ByteBufException       EXPANSION_FAILED      = EXPANSION_FAILED();
    static final        int                    ADDRESS_BITS_PER_WORD = 6;

    private final int[]            blocks;
    private final long[]           frees;
    private final int              unit;
    private final long             capacity;
    private final int              b_capacity;
    private final int              memory_type;
    private final ByteBufAllocator unpooled;
    private final ReentrantLock    lock = new ReentrantLock();
    private       int              mark;
    private       long             address;
    private       ByteBuffer       directMemory;
    private       byte[]           heapMemory;

    public PooledByteBufAllocator(ByteBufAllocatorGroup group, long capacity) {
        this.capacity = capacity;
        this.unit = group.getMemoryUnit();
        this.memory_type = group.getMemoryType();
        this.b_capacity = (int) (capacity / unit);
        this.frees = new long[b_capacity / 8];
        this.blocks = new int[b_capacity];
        this.unpooled = UnpooledByteBufAllocator.get();
    }

    static ByteBufException EXPANSION_FAILED() {
        return Util.unknownStackTrace(new ByteBufException(), PooledByteBufAllocator.class, "expansion");
    }

    @Override
    public ByteBuf allocate() {
        return allocate(1);
    }

    @Override
    public ByteBuf allocate(int limit) {
        if (limit < 1) {
            return ByteBuf.empty();
        }
        int           size = trans_size(limit, unit);
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (!isRunning()) {
                return unpooled.allocate(limit);
            }
            int mark        = this.mark;
            int block_start = allocate(mark, b_capacity, size);
            if (block_start == -1) {
                block_start = allocate(0, mark, size);
                if (block_start != -1) {
                    ByteBuf buf = newByteBuf().produce(block_start, size);
                    debug_buf_alloc_path(buf);
                    return buf;
                }
            } else {
                ByteBuf buf = newByteBuf().produce(block_start, size);
                debug_buf_alloc_path(buf);
                return buf;
            }
        } finally {
            lock.unlock();
        }
        // FIXME 是否申请java内存
        return unpooled.allocate(limit);
    }

    private static int trans_size(int limit, int unit) {
        return (limit + unit - 1) / unit;
    }

    private static void debug_buf_alloc_path(ByteBuf buf) {
        if (Develop.BUF_PATH_DEBUG) {
            BufDebug d = new BufDebug();
            d.buf = buf;
            d.e = new Exception(DateUtil.get().formatYyyy_MM_dd_HH_mm_ss_SSS());
            BUF_DEBUGS.put(buf, d);
        }
    }

    // FIXME 判断余下的是否足够，否则退出循环
    private int allocate(int pos, int end, int size) {
        if (size == 1) {
            return allocate_single(pos, end);
        } else {
            return allocate_multiple_inline(pos, end, size);
        }
    }

    private int allocate_multiple(int pos, int end, int size) {
        int[]  blocks = this.blocks;
        long[] frees  = this.frees;
        int    limit  = pos + size;
        if (limit > end) {
            return -1;
        }
        for (; ; ) {
            int free_index = scan_free(frees, pos, limit);
            if (free_index == limit) {
                int b_start = free_index - size;
                clearFree(frees, b_start);
                blocks[b_start] = free_index;
                this.mark = free_index;
                return b_start;
            } else {
                pos = blocks[free_index];
                limit = pos + size;
                if (limit > end) {
                    return -1;
                }
            }
        }
    }

    private int allocate_multiple_inline(int pos, int end, int size) {
        int[]  blocks = this.blocks;
        long[] frees  = this.frees;
        int    limit  = pos + size;
        if (limit > end) {
            return -1;
        }
        RETRY:
        for (; ; ) {
            for (; pos < limit; pos++) {
                if (!isFree(frees, pos)) {
                    pos = blocks[pos];
                    limit = pos + size;
                    if (limit > end) {
                        return -1;
                    } else {
                        continue RETRY;
                    }
                }
            }
            int b_start = limit - size;
            clearFree(frees, b_start);
            blocks[b_start] = limit;
            this.mark = limit;
            return b_start;
        }
    }

    private int scan_free(long[] frees, int start, int limit) {
        for (int i = start; i < limit; i++) {
            if (!isFree(frees, i)) {
                return i;
            }
        }
        return limit;
    }

    private int allocate_single(int pos, int end) {
        long[] frees  = this.frees;
        int[]  blocks = this.blocks;
        if (pos < end) {
            if (isFree(frees, pos)) {
                int block_end = pos + 1;
                clearFree(frees, pos);
                blocks[pos] = block_end;
                this.mark = block_end;
                return pos;
            }
            for (int i = pos + 1; i < end; ) {
                if (isFree(frees, i)) {
                    int block_end = i + 1;
                    clearFree(frees, i);
                    blocks[i] = block_end;
                    this.mark = block_end;
                    return i;
                } else {
                    i = blocks[i];
                }
            }
        }
        return -1;
    }

    @Override
    protected void doStart() {
        Arrays.fill(blocks, 0);
        Arrays.fill(frees, -1);
        if (Develop.BUF_DEBUG) {
            if (memory_type == Unsafe.BUF_UNSAFE) {
                this.address = Unsafe.allocate(capacity);
            } else if (memory_type == Unsafe.BUF_DIRECT) {
                this.directMemory = Unsafe.allocateDirectByteBuffer((int) capacity);
                this.address = Unsafe.address(directMemory);
            } else if (memory_type == Unsafe.BUF_HEAP) {
                byte[] memory = this.heapMemory;
                if (memory == null || memory.length != capacity) {
                    this.address = -1;
                    this.heapMemory = new byte[(int) capacity];
                }
            } else {
                throw new IllegalArgumentException("memory type: " + memory_type);
            }
        } else {
            if (Unsafe.UNSAFE_BUF_AVAILABLE) {
                this.address = Unsafe.allocate(capacity);
            } else if (Unsafe.DIRECT_BUFFER_AVAILABLE) {
                this.directMemory = Unsafe.allocateDirectByteBuffer((int) capacity);
                this.address = Unsafe.address(directMemory);
            } else {
                byte[] memory = this.heapMemory;
                if (memory == null || memory.length != capacity) {
                    this.address = -1;
                    this.heapMemory = new byte[(int) capacity];
                }
            }
        }
    }

    private static int wordIndex(int bitIndex) {
        return bitIndex >> ADDRESS_BITS_PER_WORD;
    }

    private static void setFree(long[] frees, int index) {
        int wordIndex = wordIndex(index);
        frees[wordIndex] |= (1L << index);
    }

    private static void clearFree(long[] frees, int index) {
        int wordIndex = wordIndex(index);
        frees[wordIndex] &= ~(1L << index);
    }

    private static boolean isFree(long[] frees, int index) {
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
                int size  = (cap + unit - 1) / unit;
                int start = buf.unitOffset();
                int alloc = expansion_alloc(start, size);
                if (alloc == start) {
                    buf.capacity(size * unit);
                } else {
                    expansion_and_copy(buf, alloc, size);
                }
            } finally {
                lock.unlock();
            }
        }
    }

    private int expansion_alloc(int start, int size) {
        setFree(frees, start);
        int alloc = allocate(start, b_capacity, size);
        if (alloc == -1) {
            alloc = allocate(0, start, size);
            if (alloc == -1) {
                clearFree(frees, start);
                throw EXPANSION_FAILED;
            }
        }
        return alloc;
    }

    private void expansion_and_copy(ByteBuf buf, int alloc, int size) {
        int read_index  = buf.readIndex();
        int write_index = buf.writeIndex();
        int offset      = buf.offset();
        buf.produce(alloc, size);
        if (Develop.BUF_DEBUG) {
            if (memory_type == Unsafe.BUF_UNSAFE) {
                Unsafe.copyMemory(address + offset, address + buf.offset(), write_index);
            } else if (memory_type == Unsafe.BUF_DIRECT) {
                Unsafe.copyMemory(address + offset, address + buf.offset(), write_index);
            } else {
                System.arraycopy(heapMemory, offset, heapMemory, buf.offset(), write_index);
            }
        } else {
            if (Unsafe.UNSAFE_BUF_AVAILABLE) {
                Unsafe.copyMemory(address + offset, address + buf.offset(), write_index);
            } else if (Unsafe.DIRECT_BUFFER_AVAILABLE) {
                Unsafe.copyMemory(address + offset, address + buf.offset(), write_index);
            } else {
                System.arraycopy(heapMemory, offset, heapMemory, buf.offset(), write_index);
            }
        }
        buf.readIndex(read_index).writeIndex(write_index);
    }

    @Override
    public void freeMemory() {
        if (Develop.BUF_DEBUG) {
            if (memory_type == Unsafe.BUF_UNSAFE) {
                Unsafe.free(address);
            } else if (memory_type == Unsafe.BUF_DIRECT) {
                Unsafe.freeByteBuffer(directMemory);
            } else {
                // FIXME 这里不free了，如果在次申请的时候大小和这次一致，则不再重新申请
                // this.memory = null;
            }
        } else {
            if (Unsafe.UNSAFE_BUF_AVAILABLE) {
                Unsafe.free(address);
            } else if (Unsafe.DIRECT_BUFFER_AVAILABLE) {
                Unsafe.freeByteBuffer(directMemory);
            } else {
                // FIXME 这里不free了，如果在次申请的时候大小和这次一致，则不再重新申请
                // this.memory = null;
            }
        }
    }

    public long getAddress() {
        return address;
    }

    @Override
    public final long getCapacity() {
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
        state.free = b_capacity - usedMem();
        state.memory = b_capacity;
        state.mfree = maxFree();
        return state;
    }

    @Override
    public final int getUnit() {
        return unit;
    }

    public ByteBuf getUsedBuf(int skip) {
        int    skiped = 0;
        long[] frees  = this.frees;
        for (int i = 0; i < b_capacity; i++) {
            if (!isFree(frees, i)) {
                skiped++;
                if (skiped > skip) {
                    return newByteBuf().produce(i, blocks[i] - i);
                }
            }
        }
        return null;
    }

    private int maxFree() {
        int    free    = 0;
        int    maxFree = 0;
        long[] frees   = this.frees;
        for (int i = 0; i < b_capacity; i++) {
            if (isFree(frees, i)) {
                free++;
            } else {
                maxFree = Math.max(maxFree, free);
                i = blocks[i] - 1;
                free = 0;
            }
        }
        return Math.max(maxFree, free);
    }

    private ByteBuf newByteBuf() {
        if (Develop.BUF_DEBUG) {
            if (memory_type == Unsafe.BUF_UNSAFE) {
                return new PooledUnsafeByteBuf(this, address);
            } else if (memory_type == Unsafe.BUF_DIRECT) {
                return new PooledDirectByteBuf(this, directMemory.duplicate());
            } else {
                return new PooledHeapByteBuf(this, heapMemory);
            }
        } else {
            if (Unsafe.UNSAFE_BUF_AVAILABLE) {
                return new PooledUnsafeByteBuf(this, address);
            } else if (Unsafe.DIRECT_BUFFER_AVAILABLE) {
                return new PooledDirectByteBuf(this, directMemory.duplicate());
            } else {
                return new PooledHeapByteBuf(this, heapMemory);
            }
        }
    }

    @Override
    public void release(ByteBuf buf) {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            setFree(frees, buf.unitOffset());
        } finally {
            lock.unlock();
        }
        debug_buf_alloc_path_remove(buf);
    }

    private static void debug_buf_alloc_path_remove(ByteBuf buf) {
        if (Develop.BUF_PATH_DEBUG && buf.isPooled()) {
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
            b.append(",type=");
            b.append(Unsafe.getMemoryType());
            b.append("]");
            return b.toString();
        } finally {
            lock.unlock();
        }
    }

    private int usedBuf() {
        int    used  = 0;
        long[] frees = this.frees;
        for (int i = 0; i < b_capacity; i++) {
            if (!isFree(frees, i)) {
                used++;
            }
        }
        return used;
    }

    private int usedMem() {
        int    used  = 0;
        long[] frees = this.frees;
        for (int i = 0; i < b_capacity; i++) {
            if (!isFree(frees, i)) {
                int next = blocks[i];
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
