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

/**
 * @author wangkai
 *
 */
class UnpooledHeapByteBuf extends AbstractHeapByteBuf {

    UnpooledHeapByteBuf(ByteBufAllocator allocator, byte[] memory) {
        super(allocator, memory);
        this.produce(memory.length);
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

    protected UnpooledHeapByteBuf produce(ByteBuf buf) {
        this.capacity = buf.capacity();
        this.limit = buf.limit();
        this.position = buf.position();
        this.referenceCount = 1;
        return this;
    }
    
    protected void produce(int capacity) {
        this.capacity = capacity;
        this.limit = capacity;
        this.referenceCount = 1;
    }

    @Override
    public void release() {}

}
