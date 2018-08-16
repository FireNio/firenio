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

/**
 * @author wangkai
 *
 */
//FIXME ..make this readonly
public class DuplicatedDirectByteBuf extends AbstractDirectByteBuf {

    private ByteBuf proto;

    public DuplicatedDirectByteBuf(ByteBuffer memory, ByteBuf proto) {
        super(null, memory);
        this.proto = proto;
    }

    @Override
    public ByteBuf duplicate() {
        return proto.duplicate();
    }
    
    @Override
    public void release() {
        proto.release();
    }

    @Override
    public PooledByteBuf newByteBuf(ByteBufAllocator allocator) {
        throw new UnsupportedOperationException();
    }

    protected ByteBuf produce(ByteBuf buf) {
        this.offset = buf.offset();
        this.capacity = buf.capacity();
        this.limit(buf.limit());
        this.position(buf.position());
        this.referenceCount = 1;
        return this;
    }

}