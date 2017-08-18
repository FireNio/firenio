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

public class HeapByteBufFactory implements ByteBufFactory {

    private byte[] memory = null;

    @Override
    public void freeMemory() {
        //FIXME 这里不free了，如果在次申请的时候大小和这次一致，则不在重新申请
        //		this.memory = null;
    }

    @Override
    public void initializeMemory(int capacity) {
        if (memory != null && memory.length == capacity) {
            return;
        }
        this.memory = new byte[capacity];
    }

    /**
     * @return the memory
     */
    public byte[] getMemory() {
        return memory;
    }

    @Override
    public PooledByteBuf newByteBuf(ByteBufAllocator allocator) {
        return new PooledHeapByteBuf(allocator, memory);
    }

}
