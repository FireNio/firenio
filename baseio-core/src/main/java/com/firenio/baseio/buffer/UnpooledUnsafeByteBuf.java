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

import com.firenio.baseio.common.Unsafe;

/**
 * @author wangkai
 *
 */
final class UnpooledUnsafeByteBuf extends UnsafeByteBuf {

    UnpooledUnsafeByteBuf(long memory, int cap) {
        super(memory);
        this.capacity = cap;
        this.referenceCount = 1;
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
        return new DuplicatedUnsafeByteBuf(this, 1);
    }

    @Override
    public void expansion(int cap) {
        long oldBuffer = memory;
        try {
            long newBuffer = Unsafe.allocate(cap);
            int pos = absPos();
            if (pos > 0) {
                copy(oldBuffer + pos, newBuffer, pos);
            }
            memory = newBuffer;
        } finally {
            Unsafe.free(oldBuffer);
        }
    }

    @Override
    protected void release0() {
        Unsafe.free(address());
    }

}
