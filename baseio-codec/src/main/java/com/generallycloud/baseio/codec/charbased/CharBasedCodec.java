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
package com.generallycloud.baseio.codec.charbased;

import java.io.IOException;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.ByteBufAllocator;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.protocol.Future;
import com.generallycloud.baseio.protocol.ProtocolCodec;

/**
 * @author wangkai
 *
 */
public class CharBasedCodec implements ProtocolCodec {

    private int  limit;

    private byte splitor;

    public CharBasedCodec() {
        this(1024 * 8, (byte) '\n');
    }

    public CharBasedCodec(byte splitor) {
        this(1024 * 8, splitor);
    }

    public CharBasedCodec(int limit, byte splitor) {
        this.limit = limit;
        this.splitor = splitor;
    }

    @Override
    public Future createPINGPacket(NioSocketChannel channel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Future createPONGPacket(NioSocketChannel channel, Future ping) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Future decode(NioSocketChannel channel, ByteBuf buffer) throws IOException {
        return new CharBasedFuture(limit, splitor);
    }

    @Override
    public void encode(NioSocketChannel channel, Future future) throws IOException {
        ByteBufAllocator allocator = channel.allocator();
        CharBasedFuture f = (CharBasedFuture) future;
        int writeSize = f.getWriteSize();
        if (writeSize == 0) {
            throw new IOException("null write buffer");
        }
        ByteBuf buf = allocator.allocate(writeSize + 1);
        buf.put(f.getWriteBuffer(), 0, writeSize);
        buf.putByte(splitor);
        future.setByteBuf(buf.flip());
    }

    @Override
    public String getProtocolId() {
        return "LineBased";
    }

    @Override
    public void initialize(ChannelContext context) {}

}
