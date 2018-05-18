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

/**
 * @author wangkai
 *
 */
public abstract class AbstractByteBufFactory implements ByteBufFactory{

    private int             bufIndex = 0;
    private PooledByteBuf[] bufs     = new PooledByteBuf[1024 * 8];

    @Override
    public PooledByteBuf newByteBuf(ByteBufAllocator allocator) {
        if (bufIndex == 0) {
            return newByteBuf0(allocator);
        }
        return bufs[--bufIndex];
    }
    
    abstract PooledByteBuf newByteBuf0(ByteBufAllocator allocator) ;

    @Override
    public void freeBuf(PooledByteBuf buf) {
        if (bufIndex == bufs.length) {
            return;
        }
        bufs[bufIndex++] = buf;
    }
    
}
