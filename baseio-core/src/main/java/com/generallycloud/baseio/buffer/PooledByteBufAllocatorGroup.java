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

import com.generallycloud.baseio.AbstractLifeCycle;
import com.generallycloud.baseio.LifeCycleUtil;
import com.generallycloud.baseio.component.NioEventLoopGroup;

public class PooledByteBufAllocatorGroup extends AbstractLifeCycle
        implements ByteBufAllocatorGroup {

    private PooledByteBufAllocator[] allocators;
    private PooledByteBufAllocator   allocator;
    private NioEventLoopGroup        group;

    public PooledByteBufAllocatorGroup(NioEventLoopGroup group) {
        this.group = group;
    }

    @Override
    protected void doStart() throws Exception {
        if (allocators == null) {
            int core = group.getEventLoopSize();
            int cap = group.getMemoryPoolCapacity();
            int unit = group.getMemoryPoolUnit();
            boolean direct = group.isEnableMemoryPoolDirect();
            this.allocators = new PooledByteBufAllocator[core];
            for (int i = 0; i < allocators.length; i++) {
                allocators[i] = new PooledByteBufAllocator(cap, unit, direct);
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
    protected void doStop() throws Exception {
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
