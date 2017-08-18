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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.baseio.common.ReleaseUtil;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

/**
 * @author wangkai
 *
 */
public abstract class AbstractPooledByteBufAllocator extends AbstractByteBufAllocator {

    protected int               capacity;

    protected int               mask;

    protected int               unitMemorySize;

    protected ByteBufFactory    bufFactory;

    protected ReentrantLock     lock;

    protected List<ByteBufUnit> busyUnit = new ArrayList<>();

    protected Logger            logger   = LoggerFactory
            .getLogger(AbstractPooledByteBufAllocator.class);

    public AbstractPooledByteBufAllocator(int capacity, int unitMemorySize, boolean isDirect) {
        super(isDirect);
        this.capacity = capacity;
        this.unitMemorySize = unitMemorySize;
    }

    @Override
    public ByteBuf allocate(int limit) {
        return allocate(bufFactory, limit);
    }

    private PooledByteBuf allocate(ByteBufNew byteBufNew, int limit) {

        int size = (limit + unitMemorySize - 1) / unitMemorySize;

        ReentrantLock lock = this.lock;

        lock.lock();

        try {

            if (!isRunning()) {
                return null;
            }

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
                throw new BufferException("reallocate failed");
            }

            newBuf.read(buf.flip());

            ReleaseUtil.release(buf);

            return buf.newByteBuf(this).produce(newBuf);
        }

        ReleaseUtil.release(buf);

        ByteBuf newBuf = allocate(buf, limit);

        if (newBuf == null) {
            throw new BufferException("reallocate failed");
        }
        return newBuf;
    }

    @Override
    public void freeMemory() {
        bufFactory.freeMemory();
    }

    protected abstract PooledByteBuf allocate(ByteBufNew byteBufNew, int limit, int start, int end,
            int size);

    @Override
    protected void doStart() throws Exception {

        lock = new ReentrantLock();

        createBufFactory();

        int capacity = this.capacity;

        initializeMemory(capacity * unitMemorySize);

        ByteBufUnit[] bufs = createUnits(capacity);

        for (int i = 0; i < capacity; i++) {
            ByteBufUnit buf = new ByteBufUnit();
            buf.index = i;
            bufs[i] = buf;
        }

    }

    protected abstract ByteBufUnit[] createUnits(int capacity);

    protected abstract ByteBufUnit[] getUnits();

    private void createBufFactory() {
        if (isDirect) {
            if (!(bufFactory instanceof DirectByteBufFactory)) {
                bufFactory = new DirectByteBufFactory();
            }
        } else {
            if (!(bufFactory instanceof HeapByteBufFactory)) {
                bufFactory = new HeapByteBufFactory();
            }
        }
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public int getUnitMemorySize() {
        return unitMemorySize;
    }

    protected void initializeMemory(int capacity) {
        bufFactory.initializeMemory(capacity);
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

    private int fillBusy() {
        busyUnit.clear();

        ByteBufUnit[] memoryUnits = getUnits();

        int free = 0;

        for (ByteBufUnit b : memoryUnits) {

            if (b.free) {
                free++;
            } else {
                busyUnit.add(b);
            }
        }
        return free;
    }

    @Override
    public synchronized String toString() {

        int free = fillBusy();

        StringBuilder b = new StringBuilder();
        b.append(this.getClass().getSimpleName());
        b.append("[free=");
        b.append(free);
        b.append(",memory=");
        b.append(capacity);
        b.append(",isDirect=");
        b.append(isDirect);
        b.append("]");
        return b.toString();
    }

    public synchronized void printBusy() {
        fillBusy();
        HeapByteBufFactory factory = (HeapByteBufFactory) bufFactory;
        byte[] memory = factory.getMemory();
        if (busyUnit.size() == 0) {
            logger.info("no busy to print!");
        }
        for (int i = 0; i < busyUnit.size(); i++) {
            ByteBufUnit u = busyUnit.get(i);
            int off = unitMemorySize * u.index;
            int end = unitMemorySize * u.blockEnd;
            StringBuilder b = new StringBuilder((end - off) * 4);
            for (int j = off; j < end; j++) {
                b.append(memory[j]);
                b.append(',');
            }
            logger.info("busy memory:{}", b);
        }
    }

}
