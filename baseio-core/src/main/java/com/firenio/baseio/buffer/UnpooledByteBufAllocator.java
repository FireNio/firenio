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

public final class UnpooledByteBufAllocator extends ByteBufAllocator {

    private static final UnpooledByteBufAllocator directAlloc = new UnpooledByteBufAllocator(true);
    private static final UnpooledByteBufAllocator heapAlloc   = new UnpooledByteBufAllocator(false);
    private final boolean                         isDirect;

    private UnpooledByteBufAllocator(boolean isDirect) {
        this.isDirect = isDirect;
    }

    @Override
    public ByteBuf allocate() {
        return allocate(512);
    }

    @Override
    public ByteBuf allocate(int capacity) {
        if (isDirect()) {
            return ByteBuf.direct(capacity);
        } else {
            return ByteBuf.heap(capacity);
        }
    }

    @Override
    protected void doStart() throws Exception {}

    @Override
    protected void doStop() {}

    @Override
    public void expansion(ByteBuf buf, int cap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void freeMemory() {}

    @Override
    public int getCapacity() {
        return -1;
    }

    @Override
    public int getUnit() {
        return -1;
    }

    public boolean isDirect() {
        return isDirect;
    }

    @Override
    public void release(ByteBuf buf) {
        throw new UnsupportedOperationException();
    }

    public static UnpooledByteBufAllocator get(boolean direct) {
        return direct ? directAlloc : heapAlloc;
    }

    public static UnpooledByteBufAllocator getDirect() {
        return directAlloc;
    }

    public static UnpooledByteBufAllocator getHeap() {
        return heapAlloc;
    }

}
