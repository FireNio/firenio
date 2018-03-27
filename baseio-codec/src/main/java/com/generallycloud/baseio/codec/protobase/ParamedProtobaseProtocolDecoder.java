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
package com.generallycloud.baseio.codec.protobase;

import java.io.IOException;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.ByteBufAllocator;
import com.generallycloud.baseio.codec.protobase.future.ParamedProtobaseFutureImpl;
import com.generallycloud.baseio.component.SocketChannel;
import com.generallycloud.baseio.protocol.ChannelFuture;
import com.generallycloud.baseio.protocol.ProtocolDecoder;

/**
 * <pre>
 * 
 *  B0 :0-1 : 报文类型 0=UNKONW 1=NORMAL 2=PING 3=PONG
 *  B0 :2   : 推送类型 0=PUSH   1=BRODCAST
 *  B0 :3   : 是否包含FutureId  4 byte   
 *  B0 :4   : 是否包含SessionId 4 byte
 *  B0 :5   : 是否包含HashId    4 byte
 *  B0 :6   : 是否包含Binary    4 byte
 *  B0 :7   : 预留
 *  B1      : 预留
 *  B2-B3   : future name
 *  B4-B7   ：text   length
 *  
 * </pre>
 */
public class ParamedProtobaseProtocolDecoder implements ProtocolDecoder {

    protected int limit;

    public ParamedProtobaseProtocolDecoder(int limit) {
        this.limit = limit;
    }

    @Override
    public ChannelFuture decode(SocketChannel channel, ByteBuf buffer) throws IOException {
        ByteBufAllocator allocator = channel.getByteBufAllocator();
        ByteBuf buf = allocator.allocate(2);
        return new ParamedProtobaseFutureImpl(channel, buf);
    }

}
