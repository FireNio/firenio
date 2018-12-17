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
import com.firenio.baseio.component.NioEventLoopGroup;

/**
 * @author wangkai
 *
 */
public class UnpooledByteBufAllocatorGroup extends AbstractLifeCycle
        implements ByteBufAllocatorGroup {

    private ByteBufAllocator  allocator;
    private NioEventLoopGroup group;

    public UnpooledByteBufAllocatorGroup(NioEventLoopGroup group) {
        this.group = group;
    }

    @Override
    protected void doStart() throws Exception {
        if (group.isEnableMemoryPoolDirect()) {
            allocator = ByteBufUtil.direct();
        } else {
            allocator = ByteBufUtil.heap();
        }
    }

    @Override
    protected void doStop() {
        LifeCycleUtil.stop(allocator);
    }

    @Override
    public ByteBufAllocator getNext() {
        return allocator;
    }

}
