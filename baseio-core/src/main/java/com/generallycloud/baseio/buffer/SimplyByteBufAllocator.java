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

public class SimplyByteBufAllocator extends AbstractPooledByteBufAllocator {

    protected ByteBufUnit2[] units;

    public SimplyByteBufAllocator(int capacity, int unitMemorySize, boolean isDirect) {
        super(capacity, unitMemorySize, isDirect);
    }

    private String tName() {
        return Thread.currentThread().getName();
    }

    @Override
    protected ByteBufUnit2[] getUnits() {
        return units;
    }

    @Override
    protected PooledByteBuf allocate(ByteBufNew byteBufNew, int limit, int begin, int end,
            int size) {

        logger.debug("申请内存____________________________{},{}", size, tName());

        ByteBufUnit2[] units = this.units;

        for (; begin < end;) {

            ByteBufUnit2 unitBegin = units[begin];

            if (!unitBegin.free) {

                begin = unitBegin.blockEnd;

                continue;
            }

            int blockEnd = unitBegin.blockEnd;

            int blockBegin = unitBegin.blockBegin;

            int freeSize = blockEnd - blockBegin;

            if (freeSize < size) {

                begin = blockEnd;

                continue;
            }

            ByteBufUnit2 unitEnd = units[blockEnd - 1];

            blockBegin = unitEnd.blockBegin;

            blockEnd = unitEnd.blockEnd;

            if (freeSize == size) {

                logger.debug("申请内存结束____________________________{},{},{}",
                        new Object[] { unitBegin.index, unitEnd.index, tName() });

                setBlock(unitBegin, unitEnd, false);

                mask = blockEnd;

                return byteBufNew.newByteBuf(this).produce(blockBegin, blockEnd, limit);
            }

            unitBegin = units[blockBegin];

            ByteBufUnit2 buf1 = units[blockBegin + size - 1];

            ByteBufUnit2 buf2 = units[buf1.index + 1];

            setBlock(buf2, unitEnd, true);

            logger.debug("申请内存前释放____________________________{},{},{}",
                    new Object[] { buf2.index, unitEnd.index, tName() });

            setBlock(unitBegin, buf1, false);

            logger.debug("申请内存结束____________________________{},{},{}",
                    new Object[] { unitBegin.index, buf1.index, tName() });

            System.out.println();

            mask = buf2.index;

            return byteBufNew.newByteBuf(this).produce(blockBegin, blockEnd, limit);
        }

        return null;
    }

    @Override
    protected ByteBufUnit2[] createUnits(int capacity) {
        this.units = new ByteBufUnit2[capacity];
        return units;
    }

    @Override
    protected void doStart() throws Exception {

        super.doStart();

        ByteBufUnit2 begin = units[0];
        ByteBufUnit2 end = units[capacity - 1];

        setBlock(begin, end, true);
    }

    private void setBlock(ByteBufUnit2 begin, ByteBufUnit2 end, boolean free) {

        int beginIndex = begin.index;
        int endIndex = end.index + 1;

        begin.free = free;
        begin.blockBegin = beginIndex;
        begin.blockEnd = endIndex;

        end.free = free;
        end.blockBegin = beginIndex;
        end.blockEnd = endIndex;

        //		if (free) {
        //			logger.debug("free {}>{},,,,,{}",new Object[]{beginIndex,endIndex,tName()});
        //		}else{
        //			logger.debug("allocate {}>{},,,,,{}",new Object[]{beginIndex,endIndex,tName()});
        //		}
    }

    private void doRelease(ByteBufUnit2 begin) {

        ByteBufUnit2[] bufs = this.units;

        int beginIndex = begin.blockBegin;
        int endIndex = begin.blockEnd;

        ByteBufUnit2 bufBegin = begin;
        ByteBufUnit2 bufEnd = bufs[endIndex - 1];

        bufBegin.free = true;
        bufEnd.free = true;

        if (beginIndex != 0) {

            ByteBufUnit2 leftBuf = bufs[beginIndex - 1];

            if (leftBuf.free) {
                bufBegin = bufs[leftBuf.blockBegin];
            }
        }

        if (endIndex != capacity) {

            ByteBufUnit2 rightBuf = bufs[endIndex];

            if (rightBuf.free) {
                bufEnd = bufs[rightBuf.blockEnd - 1];
            }
        }

        setBlock(bufBegin, bufEnd, true);

        logger.debug("释放内存____________________________{},{},{}",
                new Object[] { bufBegin.index, bufEnd.index, tName() });
        System.out.println();
    }

    @Override
    public void release(ByteBuf buf) {

        ReentrantLock lock = this.lock;

        lock.lock();

        try {

            doRelease(getUnits()[((PooledByteBuf) buf).getBeginUnit()]);

        } finally {
            lock.unlock();
        }
    }

}
