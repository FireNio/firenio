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

import java.nio.ByteBuffer;

final class PooledDirectByteBuf extends DirectByteBuf {

    private PooledByteBufAllocator allocator;
    private int                    capacity;
    private int                    offset;
    private int                    unitOffset;

    PooledDirectByteBuf(PooledByteBufAllocator allocator, ByteBuffer memory) {
        super(memory);
        this.allocator = allocator;
    }

    @Override
    public long address() {
        return allocator.getAddress();
    }

    @Override
    public int capacity() {
        return capacity;
    }

    @Override
    protected void capacity(int cap) {
        this.capacity = cap;
    }

    @Override
    public ByteBuf duplicate() {
        if (isReleased()) {
            throw new IllegalStateException("released");
        }
        addReferenceCount();
        return new DuplicatedByteBuf(memory.duplicate(), this, 1);
    }

    @Override
    public final void expansion(int cap) {
        allocator.expansion(this, cap);
    }

    @Override
    public boolean isPooled() {
        return true;
    }

    @Override
    protected int offset() {
        return offset;
    }

    @Override
    protected void offset(int offset) {
        this.offset = offset;
    }

    @Override
    protected ByteBuf produce(int unitOffset, int unitEnd) {
        int unit = allocator.getUnit();
        this.offset = unitOffset * unit;
        this.capacity = (unitEnd - unitOffset) * unit;
        this.limit(capacity);
        this.position(0);
        this.unitOffset = unitOffset;
        this.referenceCount = 1;
        return this;
    }

    @Override
    protected final void release0() {
        allocator.release(this);
    }

    @Override
    protected int unitOffset() {
        return unitOffset;
    }

    @Override
    protected void unitOffset(int unitOffset) {
        this.unitOffset = unitOffset;
    }

}
