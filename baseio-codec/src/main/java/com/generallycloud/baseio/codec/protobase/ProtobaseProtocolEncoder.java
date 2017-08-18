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
import java.nio.charset.Charset;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.ByteBufAllocator;
import com.generallycloud.baseio.buffer.EmptyByteBuf;
import com.generallycloud.baseio.codec.protobase.future.ProtobaseFuture;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.ByteArrayBuffer;
import com.generallycloud.baseio.protocol.ChannelFuture;
import com.generallycloud.baseio.protocol.ProtocolEncoder;
import com.generallycloud.baseio.protocol.ProtocolException;

public class ProtobaseProtocolEncoder implements ProtocolEncoder {

    private static final byte[] EMPTY_ARRAY = EmptyByteBuf.getInstance().array();

    @Override
    public void encode(ByteBufAllocator allocator, ChannelFuture future) throws IOException {

        if (future.isHeartbeat()) {
            byte b = future.isPING() ? ProtobaseProtocolDecoder.PROTOCOL_PING
                    : ProtobaseProtocolDecoder.PROTOCOL_PONG;
            ByteBuf buf = allocator.allocate(1);
            buf.putByte(b);
            future.setByteBuf(buf.flip());
            return;
        }

        ProtobaseFuture f = (ProtobaseFuture) future;

        String future_name = f.getFutureName();

        if (StringUtil.isNullOrBlank(future_name)) {
            throw new ProtocolException("future name is empty");
        }

        Charset charset = future.getContext().getEncoding();

        byte[] future_name_array = future_name.getBytes(charset);

        if (future_name_array.length > 255) {
            throw new IllegalArgumentException("service name too long ," + future_name);
        }

        byte future_name_length = (byte) future_name_array.length;

        ByteArrayBuffer binary = f.getWriteBinaryBuffer();

        ByteArrayBuffer buffer = f.getWriteBuffer();

        byte[] text_array;
        int text_length;
        if (buffer == null) {
            text_array = EMPTY_ARRAY;
            text_length = 0;
        } else {
            text_array = buffer.array();
            text_length = buffer.size();
        }

        if (binary != null) {
            text_length = text_array.length;
            int header_length = getHeaderLengthWithBinary();
            int binary_length = binary.size();
            byte byte0 = getBinaryFirstByte();

            int all_length = header_length + future_name_length + text_length + binary_length;

            ByteBuf buf = allocator.allocate(all_length);

            buf.putByte(byte0);
            buf.putByte((future_name_length));
            buf.putInt(f.getFutureId());
            putHeaderExtend(f, buf);
            buf.putUnsignedShort(text_length);
            buf.putInt(binary_length);

            buf.put(future_name_array);

            if (text_length > 0) {
                buf.put(text_array, 0, text_length);
            }

            buf.put(binary.array(), 0, binary_length);

            future.setByteBuf(buf.flip());
            return;
        }

        int header_length = getHeaderLengthNoBinary();

        byte byte0 = getFirstByte();

        int all_length = header_length + future_name_length + text_length;

        ByteBuf buf = allocator.allocate(all_length);

        buf.putByte(byte0);
        buf.putByte(future_name_length);
        buf.putInt(f.getFutureId());
        putHeaderExtend(f, buf);
        buf.putUnsignedShort(text_length);

        buf.put(future_name_array);

        if (text_length > 0) {
            buf.put(text_array, 0, text_length);
        }

        future.setByteBuf(buf.flip());
    }

    protected void putHeaderExtend(ProtobaseFuture future, ByteBuf buf) {

    }

    private byte getFirstByte() {
        return ProtobaseProtocolDecoder.PROTOCOL_PACKET;
    }

    private byte getBinaryFirstByte() {
        return ProtobaseProtocolDecoder.PROTOCOL_HAS_BINARY
                | ProtobaseProtocolDecoder.PROTOCOL_PACKET;
    }

    protected int getHeaderLengthNoBinary() {
        return ProtobaseProtocolDecoder.PROTOCOL_HEADER_NO_BINARY;
    }

    protected int getHeaderLengthWithBinary() {
        return ProtobaseProtocolDecoder.PROTOCOL_HEADER_WITH_BINARY;
    }

}
