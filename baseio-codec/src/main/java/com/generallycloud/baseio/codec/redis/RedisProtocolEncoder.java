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
package com.generallycloud.baseio.codec.redis;

import java.io.IOException;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.ByteBufAllocator;
import com.generallycloud.baseio.buffer.UnpooledByteBufAllocator;
import com.generallycloud.baseio.codec.redis.future.RedisFuture;
import com.generallycloud.baseio.component.ByteArrayBuffer;
import com.generallycloud.baseio.protocol.ChannelFuture;
import com.generallycloud.baseio.protocol.ProtocolEncoder;

public class RedisProtocolEncoder implements ProtocolEncoder {

    @Override
    public void encode(ByteBufAllocator allocator, ChannelFuture future) throws IOException {

        RedisFuture f = (RedisFuture) future;

        ByteArrayBuffer buffer = f.getWriteBuffer();

        if (buffer == null) {
            throw new IOException("null write text");
        }

        ByteBuf buf = UnpooledByteBufAllocator.getHeapInstance().wrap(buffer.array(), 0,
                buffer.size());

        future.setByteBuf(buf);
    }

}
