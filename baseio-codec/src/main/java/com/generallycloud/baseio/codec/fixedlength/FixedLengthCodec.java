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
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.protocol.Frame;
import com.generallycloud.baseio.protocol.ProtocolCodec;

/**
 * <pre>
 *  B0 - B3：
 *  +-----------------+-----------------+-----------------+-----------------+
 *  |        B0                B1                B2               B3        |
 *  + - - - - - - - - + - - - - - - - - + - - - - - - - - + - - - - - - - - +
 *  | 0 1 2 3 4 5 6 7   0 1 2 3 4 5 6 7   0 1 2 3 4 5 6 7   0 1 2 3 4 5 6 7 |
 *  | - - - - - - - - + - - - - - - - - + - - - - - - - - + - - - - - - - - +
 *  |                                                                       |
 *  |                          Data-length(P0-P31)                          |
 *  |                                                                       |
 *  |                                                                       |
 *  +-----------------+-----------------+-----------------+-----------------+
 *  
 *  Data-length:-1表示心跳PING,-2表示心跳PONG,正数为报文长度
 *  注意: 无论是否是心跳报文，报文头长度固定为4个字节
 * 
 * </pre>
 */
public class FixedLengthCodec extends ProtocolCodec {

    private static final ByteBuf PING;
    private static final ByteBuf PONG;
    public static final int      PROTOCOL_HEADER = 4;
    public static final int      PROTOCOL_PING   = -1;
    public static final int      PROTOCOL_PONG   = -2;

    static {
        ByteBufAllocator allocator = UnpooledByteBufAllocator.getHeap();
        PING = allocator.allocate(4);
        PONG = allocator.allocate(4);
        PING.putInt(PROTOCOL_PING);
        PONG.putInt(PROTOCOL_PONG);
        PING.flip();
        PONG.flip();
    }
    private int limit;

    public FixedLengthCodec() {
        this(1024 * 8);
    }

    public FixedLengthCodec(int limit) {
        this.limit = limit;
    }

    @Override
    public Frame ping(NioSocketChannel ch) {
        return new FixedLengthFrame().setPing();
    }

    @Override
    public Frame decode(NioSocketChannel ch, ByteBuf buffer) {
        return new FixedLengthFrame(limit);
    }

    @Override
    public ByteBuf encode(NioSocketChannel ch, Frame frame) throws IOException {
        if (frame.isSilent()) {
            return frame.isPing() ? PING.duplicate() : PONG.duplicate();
        }
        FixedLengthFrame f = (FixedLengthFrame) frame;
        int writeSize = f.getWriteSize();
        if (writeSize == 0) {
            throw new IOException("null write buffer");
        }
        ByteBuf buf = ch.alloc().allocate(writeSize + 4);
        buf.putInt(writeSize);
        buf.put(f.getWriteBuffer(), 0, writeSize);
        return buf.flip();
    }

    @Override
    public String getProtocolId() {
        return "FixedLength";
    }

}
