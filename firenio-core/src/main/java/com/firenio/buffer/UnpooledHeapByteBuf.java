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

/**
 * @author wangkai
 */
class UnpooledHeapByteBuf extends HeapByteBuf {

    UnpooledHeapByteBuf(byte[] memory, int off, int len) {
        super(memory);
        this.abs_read_index = off;
        this.abs_write_index = off;
    }

    UnpooledHeapByteBuf(ByteBuffer memory) {
        super(memory);
        this.abs_read_index = memory.position();
        this.abs_write_index = memory.position();
    }

    @Override
    public long address() {
        return -1;
    }

    @Override
    public int capacity() {
        return memory.length;
    }

    @Override
    public void expansion(int cap) {
        byte[] oldBuffer = memory;
        byte[] newBuffer = new byte[cap];
        if (hasReadableBytes()) {
            copy(oldBuffer, absReadIndex(), newBuffer, 0, readableBytes());
        }
        memory = newBuffer;
    }

    @Override
    public boolean isPooled() {
        return false;
    }

    @Override
    protected final void release0() {}

    @Override
    public void reset(ByteBuffer memory) {
        if (memory.isDirect()) {
            throw unsupportedOperationException();
        }
        this.referenceCount = 1;
        this.memory = memory.array();
        this.nioBuffer = memory;
        this.writeIndex(memory.limit());
        this.reverseRead();
    }

    @Override
    public void reset(byte[] memory, int off, int len) {
        this.referenceCount = 1;
        this.memory = memory;
        this.readIndex(off);
        this.writeIndex(off + len);
    }

    static final class EmptyByteBuf extends UnpooledHeapByteBuf {

        static final EmptyByteBuf EMPTY = new UnpooledHeapByteBuf.EmptyByteBuf();

        EmptyByteBuf() {
            super(new byte[]{}, 0, 0);
        }

        @Override
        public ByteBuf duplicate() {
            return this;
        }

        @Override
        public boolean isReleased() {
            return true;
        }

        @Override
        public void expansion(int cap) {
            throw unsupportedOperationException();
        }

        @Override
        public ByteBuf resetReadIndex() {
            throw unsupportedOperationException();
        }

        @Override
        public ByteBuf resetWriteIndex() {
            throw unsupportedOperationException();
        }

        @Override
        public ByteBuf readIndex(int index) {
            throw unsupportedOperationException();
        }

        @Override
        public ByteBuf writeIndex(int index) {
            throw unsupportedOperationException();
        }

    }

}
