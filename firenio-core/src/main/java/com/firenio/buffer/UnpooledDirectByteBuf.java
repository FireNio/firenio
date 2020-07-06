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
final class UnpooledDirectByteBuf extends DirectByteBuf {

    UnpooledDirectByteBuf(ByteBuffer memory) {
        super(memory);
        this.referenceCount = 1;
        this.abs_read_index = 0;
        this.abs_write_index = memory.position();
    }

    @Override
    public int capacity() {
        return memory.capacity();
    }

    @Override
    public void expansion(int cap) {
        ByteBuffer oldBuffer = getNioBuffer();
        try {
            ByteBuffer newBuffer       = Unsafe.allocateDirectByteBuffer(cap);
            int        old_write_index = writeIndex();
            if (old_write_index > 0) {
                copy(address(), Unsafe.address(newBuffer), old_write_index);
            }
            setMemory(newBuffer);
        } finally {
            Unsafe.freeByteBuffer(oldBuffer);
        }
    }

    @Override
    public boolean isPooled() {
        return false;
    }

    @Override
    protected void release0() {
        Unsafe.freeByteBuffer(memory);
    }

    @Override
    public void reset(ByteBuffer memory) {
        if (!memory.isDirect()){
            throw unsupportedOperationException();
        }
        this.referenceCount = 1;
        this.setMemory(memory);
        this.writeIndex(memory.limit());
        this.reverseRead();
    }

}
