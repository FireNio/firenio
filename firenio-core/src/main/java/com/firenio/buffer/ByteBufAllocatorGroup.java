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

import com.firenio.LifeCycle;
import com.firenio.common.Unsafe;
import com.firenio.common.Util;

/**
 * @author wangkai
 */
public final class ByteBufAllocatorGroup extends LifeCycle {

    private final PooledByteBufAllocator[] allocators;
    private final long                     capacity;
    private final int                      groupSize;
    private final int                      memoryUnit;
    private final int                      memoryType;

    //    public ByteBufAllocatorGroup(int groupSize, long capacity, int memoryUnit) {
    //        this(groupSize, capacity, memoryUnit, Unsafe.getMemoryTypeId());
    //    }

    public ByteBufAllocatorGroup(int groupSize, long capacity, int memoryUnit, int memoryType) {
        this.groupSize = groupSize;
        this.capacity = capacity;
        this.memoryUnit = memoryUnit;
        this.memoryType = memoryType;
        this.allocators = new PooledByteBufAllocator[groupSize];
    }

    @Override
    protected void doStart() throws Exception {
        int                      allocCapacity = (int) (this.capacity / this.groupSize);
        PooledByteBufAllocator[] allocators    = this.allocators;
        for (int i = 0; i < allocators.length; i++) {
            if (memoryType == Unsafe.BUF_HEAP) {
                allocators[i] = new HeapPooledByteBufAllocator(this, allocCapacity);
            } else {
                allocators[i] = new DirectPooledByteBufAllocator(this, allocCapacity);
            }
        }
        for (int i = 0; i < allocators.length; i++) {
            Util.start(allocators[i]);
        }
    }

    @Override
    protected void doStop() {
        for (PooledByteBufAllocator allocator : allocators) {
            Util.stop(allocator);
        }
    }

    public PooledByteBufAllocator getAllocator(int index) {
        return allocators[index];
    }

    public long getCapacity() {
        return capacity;
    }

    public int getGroupSize() {
        return groupSize;
    }

    public int getMemoryUnit() {
        return memoryUnit;
    }

    protected int getMemoryType() {
        return memoryType;
    }

    public String[] toDebugString() {
        String[] res = new String[groupSize];
        for (int i = 0; i < res.length; i++) {
            res[i] = allocators[i].toDebugString();
        }
        return res;
    }

}
