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

import java.nio.ByteBuffer;

/**
 * @author wangkai
 *
 */
class UnpooledHeapByteBuf extends HeapByteBuf {

    UnpooledHeapByteBuf(ByteBufAllocator allocator, byte[] memory, int off, int len) {
        super(allocator, memory);
        this.position = off;
        this.limit = off + len;
    }

    UnpooledHeapByteBuf(ByteBufAllocator allocator, ByteBuffer memory) {
        super(allocator, memory);
        this.position = memory.position();
        this.limit = memory.limit();
    }

    @Override
    public int capacity() {
        return memory.length;
    }

    @Override
    public ByteBuf duplicate() {
        return new DuplicatedByteBuf(nioBuffer().duplicate(), this, 0);
    }

    @Override
    public void expansion(int cap) {
        byte[] oldBuffer = memory;
        byte[] newBuffer = new byte[cap];
        if (position > 0) {
            copy(oldBuffer, 0, newBuffer, 0, position);
        }
        memory = newBuffer;
        limit = capacity();
    }

    @Override
    protected final void release0() {}

}
