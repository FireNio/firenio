/*
 * Copyright 2015 The FireNio Project
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
package com.firenio.codec.lengthvalue;

import java.io.IOException;

import com.firenio.buffer.ByteBuf;
import com.firenio.component.Frame;
import com.firenio.component.Channel;
import com.firenio.component.ProtocolCodec;

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
public final class LengthValueCodec extends ProtocolCodec {

    public static final IOException ILLEGAL_PROTOCOL = EXCEPTION("illegal protocol");
    public static final IOException OVER_LIMIT       = EXCEPTION("over limit");
    static final        ByteBuf     PING;
    static final        ByteBuf     PONG;
    static final        int         PROTOCOL_HEADER  = 4;
    static final        int         PROTOCOL_PING    = -1;
    static final        int         PROTOCOL_PONG    = -2;

    static {
        PING = ByteBuf.direct(4);
        PONG = ByteBuf.direct(4);
        PING.putInt(PROTOCOL_PING);
        PONG.putInt(PROTOCOL_PONG);
        PING.flip();
        PONG.flip();
    }

    private int limit;

    public LengthValueCodec() {
        this(1024 * 8);
    }

    public LengthValueCodec(int limit) {
        this.limit = limit;
    }

    @Override
    public Frame decode(Channel ch, ByteBuf src) throws IOException {
        if (src.remaining() < PROTOCOL_HEADER) {
            return null;
        }
        int len = src.getInt();
        if (len < 0) {
            return decode_ping(ch, len);
        }
        if (len > limit) {
            throw OVER_LIMIT;
        }
        if (len > src.remaining()) {
            src.skip(-PROTOCOL_HEADER);
            return null;
        }
        byte[] data = new byte[len];
        src.getBytes(data);
        return new LengthValueFrame(new String(data, ch.getCharset()));
    }

    private Frame decode_ping(Channel ch, int type) throws IOException {
        if (type == PROTOCOL_PING) {
            log_ping_from(ch);
            flush_pong(ch, PONG.duplicate());
        } else if (type == PROTOCOL_PONG) {
            log_pong_from(ch);
        } else {
            throw ILLEGAL_PROTOCOL;
        }
        return null;
    }

    @Override
    protected ByteBuf getPingBuf() {
        return PING.duplicate();
    }

    @Override
    public ByteBuf encode(Channel ch, Frame frame) {
        ByteBuf buf = frame.getBufContent().flip();
        buf.putInt(0, buf.limit() - PROTOCOL_HEADER);
        return buf;
    }

    @Override
    public String getProtocolId() {
        return "LengthValue";
    }

    @Override
    public int getHeaderLength() {
        return PROTOCOL_HEADER;
    }

}
