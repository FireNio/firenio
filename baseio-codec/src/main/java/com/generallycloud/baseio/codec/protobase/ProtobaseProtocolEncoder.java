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
import com.generallycloud.baseio.buffer.UnpooledByteBufAllocator;
import com.generallycloud.baseio.codec.protobase.future.ProtobaseFuture;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.ByteArrayBuffer;
import com.generallycloud.baseio.component.SocketChannel;
import com.generallycloud.baseio.protocol.ChannelFuture;
import com.generallycloud.baseio.protocol.ProtocolEncoder;
import com.generallycloud.baseio.protocol.ProtocolException;

public class ProtobaseProtocolEncoder implements ProtocolEncoder {

    private static final ByteBuf PING;
    private static final ByteBuf PONG;
    
    static{
        ByteBufAllocator allocator = UnpooledByteBufAllocator.getHeap();
        PING = allocator.allocate(2);
        PONG = allocator.allocate(2);
        PING.putByte((byte)0b10000000);
        PING.putByte((byte)0b00000000);
        PONG.putByte((byte)0b11000000);
        PONG.putByte((byte)0b00000000);
        PING.flip();
        PONG.flip();
    }

    @Override
    public void encode(SocketChannel channel, ChannelFuture future) throws IOException {
        ByteBufAllocator allocator = channel.getByteBufAllocator();
        if (future.isHeartbeat()) {
            ByteBuf buf = future.isPING() ? PING.duplicate() : PONG.duplicate();
            future.setByteBuf(buf);
            return;
        }
        ProtobaseFuture f = (ProtobaseFuture) future;
        String futureName = f.getFutureName();
        if (StringUtil.isNullOrBlank(futureName)) {
            throw new ProtocolException("future name is empty");
        }
        byte [] futureNameBytes = futureName.getBytes(channel.getEncoding());
        if (futureNameBytes.length > Byte.MAX_VALUE) {
            throw new ProtocolException("future name max length 127");
        }
        byte futureNameLength = (byte) futureNameBytes.length;
        int allLen = 6 + futureNameLength;
        byte h1 = 0b01000000;
        if (f.isBroadcast()) {
            h1 |= 0b00100000;
        }
        if (f.getFutureId() > 0) {
            h1 |= 0b00010000;
            allLen += 4;
        }
        if (f.getSessionId() > 0) {
            h1 |= 0b00001000;
            allLen += 4;
        }
        if (f.getHashCode() > 0) {
            h1 |= 0b00000100;
            allLen += 4;
        }
        if (f.getWriteBinaryBuffer() != null) {
            h1 |= 0b00000010;
            allLen += 4;
        }
        int textWriteSize = f.getWriteSize();
        if (textWriteSize > 0) {
            allLen += textWriteSize;
        }
        ByteArrayBuffer binary = f.getWriteBinaryBuffer();
        int binaryWriteSize = 0;
        if (binary != null) {
            binaryWriteSize = binary.size();
            if (binaryWriteSize > 0) {
                allLen += binaryWriteSize;
            }
        }
        ByteBuf buf = allocator.allocate(allLen);
        buf.putByte(h1);
        buf.putByte(futureNameLength);
        buf.putInt(textWriteSize);
        if (f.getFutureId() > 0) {
            buf.putInt(f.getFutureId());
        }
        if (f.getSessionId() > 0) {
            buf.putInt(f.getSessionId());
        }
        if (f.getHashCode() > 0) {
            buf.putInt(f.getHashCode());
        }
        if (binaryWriteSize > 0) {
            buf.putInt(binaryWriteSize);
        }
        buf.put(futureNameBytes);
        if (textWriteSize > 0) {
            buf.put(f.getWriteBuffer(), 0, textWriteSize);
        }
        if (binaryWriteSize > 0) {
            buf.put(binary.array(), 0, binary.size());
        }
        future.setByteBuf(buf.flip());
    }

}
