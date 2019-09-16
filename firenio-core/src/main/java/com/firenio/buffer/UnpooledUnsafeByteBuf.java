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

import com.firenio.common.Unsafe;

/**
 * @author wangkai
 */
final class UnpooledUnsafeByteBuf extends UnsafeByteBuf {

    private int capacity;

    UnpooledUnsafeByteBuf(long memory, int cap) {
        super(memory);
        this.capacity = cap;
        this.referenceCount = 1;
    }

    @Override
    public void expansion(int cap) {
        long oldBuffer = memory;
        try {
            long newBuffer = Unsafe.allocate(cap);
            if (hasReadableBytes()) {
                copy(oldBuffer + absReadIndex(), newBuffer, readableBytes());
            }
            memory = newBuffer;
        } finally {
            Unsafe.free(oldBuffer);
        }
    }

    @Override
    public int capacity() {
        return capacity;
    }

    @Override
    public boolean isPooled() {
        return false;
    }

    @Override
    protected void release0() {
        Unsafe.free(address());
    }

    @Override
    public void reset(long memory, int capacity, int off, int len) {
        this.referenceCount = 1;
        this.setMemory(memory);
        this.capacity = capacity;
        this.readIndex(off);
        this.writeIndex(off + len);
    }

}
