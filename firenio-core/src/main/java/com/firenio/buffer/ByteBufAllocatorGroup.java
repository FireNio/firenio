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
import com.firenio.common.Util;

/**
 * @author wangkai
 */
public final class ByteBufAllocatorGroup extends LifeCycle {

    private final PooledByteBufAllocator[] allocators;
    private final int                      capacity;
    private final boolean                  direct;
    private final int                      groupSize;
    private final int                      unit;

    public ByteBufAllocatorGroup() {
        this(1024 * 64);
    }

    public ByteBufAllocatorGroup(int cap) {
        this(1, cap);
    }

    public ByteBufAllocatorGroup(int groupSize, int cap) {
        this(groupSize, cap, 512, true);
    }

    public ByteBufAllocatorGroup(int groupSize, int cap, int unit, boolean direct) {
        this.groupSize = groupSize;
        this.capacity = cap;
        this.unit = unit;
        this.direct = direct;
        this.allocators = new PooledByteBufAllocator[groupSize];
        for (int i = 0; i < allocators.length; i++) {
            allocators[i] = new PooledByteBufAllocator(this);
        }
    }

    @Override
    protected void doStart() throws Exception {
        for (PooledByteBufAllocator allocator : allocators) {
            Util.start(allocator);
        }
    }

    @Override
    protected void doStop() {
        for (PooledByteBufAllocator allocator : allocators) {
            Util.stop(allocator);
        }
    }

    public int getAllCapacity() {
        return capacity * groupSize;
    }

    public PooledByteBufAllocator getAllocator(int index) {
        return allocators[index];
    }

    public int getCapacity() {
        return capacity;
    }

    public int getGroupSize() {
        return groupSize;
    }

    public int getUnit() {
        return unit;
    }

    public boolean isDirect() {
        return direct;
    }

    public String[] toDebugString() {
        String[] res = new String[groupSize];
        for (int i = 0; i < res.length; i++) {
            res[i] = allocators[i].toString();
        }
        return res;
    }

}
