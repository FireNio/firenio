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

import java.nio.ByteBuffer;

import com.generallycloud.baseio.common.ByteBufferUtil;

/**
 * @author wangkai
 *
 */
public class UnpooledDirectByteBuf extends AbstractDirectByteBuf {

    protected UnpooledDirectByteBuf(ByteBufAllocator allocator, ByteBuffer memory) {
        super(allocator, memory);
        this.produce(memory.capacity());
    }

    protected void produce(int capacity) {
        this.capacity = capacity;
        this.limit(capacity);
        this.referenceCount = 1;
    }

    @Override
    public ByteBuf reallocate(int limit, boolean copyOld) {

        ByteBuffer newMemory = ByteBuffer.allocateDirect(limit);

        if (copyOld) {
            memory.flip();
            newMemory.put(memory);
            ByteBufferUtil.release(memory);
        } else {
            this.position(0);
        }

        this.memory = newMemory;
        this.capacity = limit;
        this.limit(limit);
        this.referenceCount = 1;
        return this;
    }

    @Override
    public ByteBuf duplicate() {
        synchronized (this) {
            if (released) {
                throw new ReleasedException("released");
            }
            //请勿移除此行，DirectByteBuffer需要手动回收，doRelease要确保被执行
            this.referenceCount++;
            return new DuplicateByteBuf(new UnpooledDirectByteBuf(allocator, memory.duplicate()),
                    this);
        }
    }

    @Override
    public void doRelease() {
        ByteBufferUtil.release(memory);
    }

    @Override
    public PooledByteBuf newByteBuf(ByteBufAllocator allocator) {
        throw new UnsupportedOperationException();
    }

}
