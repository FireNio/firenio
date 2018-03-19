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
package com.generallycloud.baseio.codec.fixedlength;

import java.io.IOException;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.ByteBufAllocator;
import com.generallycloud.baseio.buffer.UnpooledByteBufAllocator;
import com.generallycloud.baseio.codec.fixedlength.future.FixedLengthFuture;
import com.generallycloud.baseio.protocol.ChannelFuture;
import com.generallycloud.baseio.protocol.ProtocolEncoder;

public class FixedLengthProtocolEncoder implements ProtocolEncoder {
    
    private static final ByteBuf PING;
    private static final ByteBuf PONG;
    
    static{
        ByteBufAllocator allocator = UnpooledByteBufAllocator.getHeap();
        PING = allocator.allocate(4);
        PONG = allocator.allocate(4);
        PING.putInt(FixedLengthProtocolDecoder.PROTOCOL_PING);
        PONG.putInt(FixedLengthProtocolDecoder.PROTOCOL_PONG);
        PING.flip();
        PONG.flip();
    }
    

    @Override
    public void encode(ByteBufAllocator allocator, ChannelFuture future) throws IOException {

        if (future.isHeartbeat()) {
            ByteBuf buf = future.isPING() ? PING.duplicate() : PONG.duplicate();
            future.setByteBuf(buf);
            return;
        }

        FixedLengthFuture f = (FixedLengthFuture) future;

        int writeSize = f.getWriteSize();

        if (writeSize == 0) {
            throw new IOException("null write buffer");
        }

        ByteBuf buf = allocator.allocate(writeSize + 4);

        buf.putInt(writeSize);

        buf.put(f.getWriteBuffer(), 0, writeSize);

        future.setByteBuf(buf.flip());
    }
}
