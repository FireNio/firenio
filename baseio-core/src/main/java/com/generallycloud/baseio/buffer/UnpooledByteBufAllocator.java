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

    private static UnpooledByteBufAllocator heapAllocator;

    private static UnpooledByteBufAllocator directAllocator;

    static {

        heapAllocator = new UnpooledByteBufAllocator(false);
        directAllocator = new UnpooledByteBufAllocator(true);

        heapAllocator.initialize();
        directAllocator.initialize();
    }

    public UnpooledByteBufAllocator(boolean isDirect) {
        super(isDirect);
    }

    private UnpooledByteBufFactory unpooledByteBufferFactory;

    public static UnpooledByteBufAllocator getHeapInstance() {
        return heapAllocator;
    }

    //FIXME 回收机制
    /**
     * 不稳定，待改进
     * @return
     */
    @Deprecated
    public static UnpooledByteBufAllocator getDirectInstance() {
        return directAllocator;
    }

    @Override
    public ByteBuf allocate(int capacity) {
        return unpooledByteBufferFactory.allocate(this, capacity);
    }

    public ByteBuf wrap(ByteBuffer buffer) {
        if (buffer.isDirect()) {
            return new UnpooledDirectByteBuf(this, buffer);
        }
        return wrap(buffer.array(), buffer.position(), buffer.remaining());
    }

    @Override
    protected void doStart() throws Exception {
        initialize();
    }

    private void initialize() {
        if (isDirect) {
            this.unpooledByteBufferFactory = new UnpooledDirectByteBufferFactory();
            return;
        }
        this.unpooledByteBufferFactory = new UnpooledHeapByteBufferFactory();
    }

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
    protected void doStop() throws Exception {
        freeMemory();
    }

    @Override
    public int getCapacity() {
        return -1;
    }

    @Override
    public ByteBuf reallocate(ByteBuf buf, int limit, boolean copyOld) {
        throw new UnsupportedOperationException();
    }

    interface UnpooledByteBufFactory {
        abstract ByteBuf allocate(ByteBufAllocator allocator, int capacity);
    }

    class UnpooledHeapByteBufferFactory implements UnpooledByteBufFactory {
        @Override
        public ByteBuf allocate(ByteBufAllocator allocator, int capacity) {
            return new UnpooledHeapByteBuf(allocator, new byte[capacity]);
        }
    }

    class UnpooledDirectByteBufferFactory implements UnpooledByteBufFactory {
        @Override
        public ByteBuf allocate(ByteBufAllocator allocator, int capacity) {
            return new UnpooledDirectByteBuf(allocator, ByteBuffer.allocateDirect(capacity));
        }
    }

}
