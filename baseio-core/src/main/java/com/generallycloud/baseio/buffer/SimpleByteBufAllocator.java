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

public class SimpleByteBufAllocator extends AbstractPooledByteBufAllocator {

    public SimpleByteBufAllocator(int capacity, int unitMemorySize, boolean isDirect) {
        super(capacity, unitMemorySize, isDirect);
    }

    protected ByteBufUnit[] units;

    //FIXME 判断余下的是否足够，否则退出循环
    @Override
    protected PooledByteBuf allocate(ByteBufNew byteBufNew, int limit, int start, int end,
            int size) {
        ByteBufUnit[] units = getUnits();
        int freeSize = 0;
        for (; start < end;) {
            ByteBufUnit unit = units[start];
            if (!unit.free) {
                start = unit.blockEnd;
                freeSize = 0;
                continue;
            }
            if (++freeSize == size) {
                int blockEnd = unit.index + 1;
                start = blockEnd - size;
                ByteBufUnit unitStart = units[start];
                ByteBufUnit unitEnd = unit;
                unitStart.free = false;
                unitStart.blockEnd = blockEnd;
                unitEnd.free = false;
                mask = blockEnd;
                return byteBufNew.newByteBuf(this).produce(start, blockEnd, limit);
            }
            start++;
        }
        return null;
    }

    @Override
    protected ByteBufUnit[] createUnits(int capacity) {
        this.units = new ByteBufUnit[capacity];
        return units;
    }

    @Override
    protected ByteBufUnit[] getUnits() {
        return units;
    }

    @Override
    public void release(ByteBuf buf) {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            ByteBufUnit[] units = getUnits();
            ByteBufUnit memoryStart = units[((PooledByteBuf) buf).getBeginUnit()];
            ByteBufUnit memoryEnd = units[memoryStart.blockEnd - 1];
            memoryStart.free = true;
            memoryEnd.free = true;
        } finally {
            lock.unlock();
        }
    }

}
