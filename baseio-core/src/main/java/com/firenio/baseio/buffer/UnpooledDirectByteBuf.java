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

import com.firenio.baseio.common.ByteUtil;
import com.firenio.baseio.common.Unsafe;

/**
 * @author wangkai
 *
 */
final class UnpooledDirectByteBuf extends DirectByteBuf {

    UnpooledDirectByteBuf(ByteBuffer memory) {
        super(memory);
        this.referenceCount = 1;
    }

    @Override
    public long address() {
        return Unsafe.address(memory);
    }

    @Override
    public int capacity() {
        return memory.capacity();
    }

    @Override
    public boolean isPooled() {
        return false;
    }

    @Override
    public ByteBuf duplicate() {
        if (isReleased()) {
            throw new IllegalStateException("released");
        }
        //请勿移除此行，DirectByteBuffer需要手动回收，release要确保被执行
        addReferenceCount();
        return new DuplicatedByteBuf(memory.duplicate(), this, 1);
    }

    @Override
    public void expansion(int cap) {
        ByteBuffer oldBuffer = memory;
        try {
            ByteBuffer newBuffer = ByteBuffer.allocateDirect(cap);
            int pos = oldBuffer.position();
            oldBuffer.position(0);
            oldBuffer.limit(pos);
            if (pos > 0) {
                copy(Unsafe.address(oldBuffer) + oldBuffer.position(),
                        Unsafe.address(newBuffer) + newBuffer.position(), pos);
            }
            newBuffer.position(pos);
            memory = newBuffer;
        } finally {
            ByteUtil.free(oldBuffer);
        }
    }

    @Override
    protected void release0() {
        ByteUtil.free(memory);
    }

}
