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

import com.firenio.baseio.AbstractLifeCycle;
import com.firenio.baseio.LifeCycleUtil;

public class PooledByteBufAllocatorGroup extends AbstractLifeCycle
        implements ByteBufAllocatorGroup {

    private PooledByteBufAllocator   allocator;
    private PooledByteBufAllocator[] allocators;
    private int                      cap    = 1024 * 64;
    private int                      core   = 1;
    private boolean                  direct = true;
    private int                      unit   = 512;

    public PooledByteBufAllocatorGroup() {}

    public PooledByteBufAllocatorGroup(int cap) {
        this.cap = cap;
    }

    public PooledByteBufAllocatorGroup(int core, int cap) {
        this.core = core;
        this.cap = cap;
    }

    public PooledByteBufAllocatorGroup(int core, int cap, int unit, boolean direct) {
        this.core = core;
        this.cap = cap;
        this.unit = unit;
        this.direct = direct;
    }

    @Override
    protected void doStart() throws Exception {
        if (allocators == null) {
            this.allocators = new PooledByteBufAllocator[core];
            int allocatorsLength = allocators.length;
            for (int i = 0; i < allocatorsLength; i++) {
                allocators[i] = new PooledByteBufAllocator(cap, unit, direct, allocatorsLength);
            }
        }
        PooledByteBufAllocator first = allocators[0];
        PooledByteBufAllocator last = allocators[0];
        LifeCycleUtil.start(first);
        for (int i = 1; i < allocators.length; i++) {
            LifeCycleUtil.start(allocators[i]);
            last.setNext(allocators[i]);
            last = allocators[i];
        }
        last.setNext(first);
        this.allocator = first;
    }

    @Override
    protected void doStop() {
        for (PooledByteBufAllocator allocator : allocators) {
            if (allocator == null) {
                continue;
            }
            LifeCycleUtil.stop(allocator);
        }
        this.allocator = null;
    }

    @Override
    public ByteBufAllocator getNext() {
        PooledByteBufAllocator next = allocator.getNext();
        this.allocator = next;
        return next;
    }

    public String toDebugString() {
        StringBuilder builder = new StringBuilder();
        for (ByteBufAllocator allocator : allocators) {
            builder.append("\n</BR>");
            builder.append(allocator.toString());
        }
        return builder.toString();
    }

}
