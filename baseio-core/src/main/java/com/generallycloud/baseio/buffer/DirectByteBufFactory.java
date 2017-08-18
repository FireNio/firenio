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

public class DirectByteBufFactory implements ByteBufFactory {

    private ByteBuffer memory = null;

    @Override
    public void initializeMemory(int capacity) {
        this.memory = ByteBuffer.allocateDirect(capacity);
    }

    @Override
    public PooledByteBuf newByteBuf(ByteBufAllocator allocator) {
        return new PooledDirectByteBuf(allocator, memory.duplicate());
    }

    @Override
    @SuppressWarnings("restriction")
    public void freeMemory() {
        if (((sun.nio.ch.DirectBuffer) memory).cleaner() != null) {
            ((sun.nio.ch.DirectBuffer) memory).cleaner().clean();
        }
    }

}
