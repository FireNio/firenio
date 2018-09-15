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

/**
 * @author wangkai
 *
 */
class UnpooledHeapByteBuf extends AbstractHeapByteBuf {

    UnpooledHeapByteBuf(ByteBufAllocator allocator, byte[] memory) {
        super(allocator, memory);
        this.produce(memory.length);
    }

    protected void produce(int capacity) {
        this.capacity = capacity;
        this.limit = capacity;
        this.referenceCount = 1;
    }

    protected UnpooledHeapByteBuf produce(ByteBuf buf) {
        this.capacity = buf.capacity();
        this.limit = buf.limit();
        this.position = buf.position();
        this.referenceCount = 1;
        return this;
    }

    @Override
    public ByteBuf reallocate(int limit, boolean copyOld) {
        if (limit <= capacity) {
            this.limit = limit;
            if (!copyOld) {
                position(0);
            }
            return this;
        }
        byte[] newMemory = new byte[limit];
        if (copyOld) {
            System.arraycopy(memory, 0, newMemory, 0, position());
        } else {
            position(0);
        }
        this.memory = newMemory;
        this.capacity = limit;
        this.limit = limit;
        return this;
    }
    
    @Override
    public ByteBuf clear() {
        this.position = 0;
        this.limit = capacity;
        return this;
    }
    
    @Override
    public ByteBuf duplicate() {
        return new DuplicatedHeapByteBuf(memory, this);
    }

    @Override
    public void release() {}

    @Override
    public PooledByteBuf newByteBuf(PooledByteBufAllocator allocator) {
        throw new UnsupportedOperationException();
    }

}
