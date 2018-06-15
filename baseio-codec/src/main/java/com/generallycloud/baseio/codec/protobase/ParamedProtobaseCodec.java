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
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.protocol.Future;

/**
 * @author wangkai
 *
 */
public class ParamedProtobaseCodec extends ProtobaseCodec {

    protected int limit;

    public ParamedProtobaseCodec() {
        this(1024 * 8);
    }

    public ParamedProtobaseCodec(int limit) {
        this.limit = limit;
    }

    @Override
    public Future createPINGPacket(NioSocketChannel channel) {
        return new ParamedProtobaseFutureImpl().setPING();
    }

    @Override
    public Future createPONGPacket(NioSocketChannel channel, Future ping) {
        return ping.setPONG();
    }

    @Override
    public Future decode(NioSocketChannel channel, ByteBuf buffer) throws IOException {
        ByteBufAllocator allocator = channel.allocator();
        ByteBuf buf = allocator.allocate(2);
        return new ParamedProtobaseFutureImpl(buf);
    }

    @Override
    public String getProtocolId() {
        return "ParamedProtobase";
    }

}
