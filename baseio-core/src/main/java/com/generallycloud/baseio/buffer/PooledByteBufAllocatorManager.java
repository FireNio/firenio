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
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.configuration.ServerConfiguration;

public class PooledByteBufAllocatorManager extends AbstractLifeCycle
        implements ByteBufAllocatorManager {

    private LinkAbleByteBufAllocator[] allocators = null;

    private LinkAbleByteBufAllocator   allocator  = null;

    private ChannelContext             context    = null;

    public PooledByteBufAllocatorManager(ChannelContext context) {
        this.context = context;
    }

    private void createByteBufAllocator(ChannelContext context) {

        if (allocators != null) {
            return;
        }

        ServerConfiguration c = context.getServerConfiguration();

        int core = c.getSERVER_CORE_SIZE();

        int capacity = c.getSERVER_MEMORY_POOL_CAPACITY();

        int unitMemorySize = c.getSERVER_MEMORY_POOL_UNIT();

        boolean direct = c.isSERVER_ENABLE_MEMORY_POOL_DIRECT();

        this.allocators = new LinkAbleByteBufAllocator[core];

        for (int i = 0; i < allocators.length; i++) {

            //			ByteBufAllocator allocator = new SimplyByteBufAllocator(capacity, unitMemorySize, direct);

            ByteBufAllocator allocator = new SimpleByteBufAllocator(capacity, unitMemorySize,
                    direct);

            //			ByteBufAllocator allocator = new UnpooledByteBufAllocator();

            allocators[i] = new LinkableByteBufAllocatorImpl(allocator, i);
        }
    }

    @Override
    protected void doStart() throws Exception {

        createByteBufAllocator(context);

        LinkAbleByteBufAllocator first = null;
        LinkAbleByteBufAllocator last = null;

        for (int i = 0; i < allocators.length; i++) {

            LinkAbleByteBufAllocator allocator = allocators[i];

            allocator.start();

            if (first == null) {
                first = allocator;
                last = allocator;
                continue;
            }

            last.setNext(allocator);

            last = allocator;
        }

        last.setNext(first);

        this.allocator = first;
    }

    @Override
    protected void doStop() throws Exception {

        for (LinkAbleByteBufAllocator allocator : allocators) {

            if (allocator == null) {
                continue;
            }

            LifeCycleUtil.stop(allocator);
        }

        this.allocator = null;
    }

    @Override
    public ByteBufAllocator getNextBufAllocator() {

        LinkAbleByteBufAllocator next = allocator.getNext();

        this.allocator = next;

        return next;
    }

    public void printBusy() {
        for (LinkAbleByteBufAllocator allocator : allocators) {
            SimpleByteBufAllocator a = (SimpleByteBufAllocator) allocator.unwrap();
            a.printBusy();
        }
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
