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

public class UnpooledByteBufAllocator extends AbstractByteBufAllocator {

    private static UnpooledByteBufAllocator heap   = new UnpooledByteBufAllocator(false);
    private static UnpooledByteBufAllocator direct = new UnpooledByteBufAllocator(true);

    private UnpooledByteBufAllocator(boolean isDirect) {
        super(isDirect);
    }

    public static UnpooledByteBufAllocator getHeap() {
        return heap;
    }

    //FIXME 回收机制
    public static UnpooledByteBufAllocator getDirect() {
        return direct;
    }

    @Override
    public ByteBuf allocate(int capacity) {
        if (isDirect()) {
            return new UnpooledDirectByteBuf(this, ByteBuffer.allocateDirect(capacity));
        } else {
            return new UnpooledHeapByteBuf(this, new byte[capacity]);
        }
    }

    public ByteBuf wrap(ByteBuffer buffer) {
        if (buffer.isDirect()) {
            return new UnpooledDirectByteBuf(this, buffer);
        }
        return wrap(buffer.array(), buffer.position(), buffer.remaining());
    }

    @Override
    protected void doStart() throws Exception {}

    public ByteBuf wrap(byte[] data) {
        return wrap(data, 0, data.length);
    }

    public ByteBuf wrap(byte[] data, int offset, int length) {
        UnpooledHeapByteBuf buf = new UnpooledHeapByteBuf(this, data);
        buf.offset = offset;
        buf.capacity = length;
        buf.limit = length;
        return buf;
    }

    @Override
    public void release(ByteBuf buf) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getUnitMemorySize() {
        return -1;
    }

    @Override
    public void freeMemory() {}

    @Override
    protected void doStop() throws Exception {}

    @Override
    public int getCapacity() {
        return -1;
    }

    @Override
    public ByteBuf reallocate(ByteBuf buf, int limit, boolean copyOld) {
        throw new UnsupportedOperationException();
    }

}
