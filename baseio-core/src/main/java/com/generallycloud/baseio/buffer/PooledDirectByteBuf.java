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

final class PooledDirectByteBuf extends AbstractDirectByteBuf implements PooledByteBuf {

    PooledDirectByteBuf(ByteBufAllocator allocator, ByteBuffer memory) {
        super(allocator, memory);
    }

    private int unitOffset;

    @Override
    public int getUnitOffset() {
        return unitOffset;
    }

    @Override
    public PooledByteBuf newByteBuf(PooledByteBufAllocator allocator) {
        this.allocator = allocator;
        this.memory = allocator.getDirectMemory().duplicate();
        return this;
    }

    @Override
    public ByteBuf duplicate() {
        if (isReleased()) {
            throw new ReleasedException("released");
        }
        addReferenceCount();
        return new DuplicatedDirectByteBuf(memory.duplicate(), this);
    }

    @Override
    public PooledByteBuf produce(int unitOffset, int unitEnd, int newLimit) {
        this.offset = unitOffset * allocator.getUnitMemorySize();
        this.capacity = (unitEnd - unitOffset) * allocator.getUnitMemorySize();
        this.limit(newLimit);
        this.position(0);
        this.unitOffset = unitOffset;
        this.referenceCount = 1;
        return this;
    }

    @Override
    public PooledDirectByteBuf produce(PooledByteBuf buf) {
        this.offset = buf.offset();
        this.capacity = buf.capacity();
        this.limit(buf.limit());
        this.position(buf.position());
        this.unitOffset = buf.getUnitOffset();
        this.referenceCount = 1;
        return this;
    }

}
