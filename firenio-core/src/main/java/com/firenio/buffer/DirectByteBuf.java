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

abstract class DirectByteBuf extends UnsafeByteBuf {

    protected ByteBuffer memory;

    DirectByteBuf(ByteBuffer memory) {
        super(Unsafe.address(memory));
        this.setMemory(memory);
    }

    @Override
    public ByteBuffer getNioBuffer() {
        return memory;
    }

    public ByteBuf reverseRead() {
        abs_read_index = getNioBuffer().position();
        return this;
    }

    public ByteBuf reverseWrite() {
        abs_write_index = getNioBuffer().position();
        return this;
    }

    protected void setMemory(ByteBuffer memory) {
        this.memory = memory;
        super.setMemory(Unsafe.address(memory));
    }

    @Override
    public ByteBuf duplicate() {
        if (isReleased()) {
            throw new IllegalStateException("released");
        }
        this.retain();
        return new DuplicatedDirectByteBuf(this, 1);
    }

}
