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
package com.firenio.codec.http11;

import java.io.IOException;

import com.firenio.buffer.ByteBuf;
import com.firenio.component.Channel;
import com.firenio.component.Frame;
import com.firenio.component.ProtocolCodec;

//FIXME 心跳貌似由服务端发起

/**
 * <pre>
 *
 *       0               1               2               3
 *       0 1 2 3 4 5 6 7 0 1 2 3 4 5 6 7 0 1 2 3 4 5 6 7 0 1 2 3 4 5 6 7
 *      +-+-+-+-+-------+-+-------------+-------------------------------+
 *      |F|R|R|R| opcode|M| Payload len |    Extended payload length    |
 *      |I|S|S|S|  (4)  |A|     (7)     |             (16/32)           |
 *      |N|V|V|V|       |S|             |   (if payload len==126/127)   |
 *      | |1|2|3|       |K|             |       (unsigned(2byte))       |
 *      +-+-+-+-+-------+-+-------------+ - - - - - - - - - - - - - - - +
 *      |     Extended payload length continued, if payload len == 127  |
 *      + - - - - - - - - - - - - - - - +-------------------------------+
 *      |    payload len (4+2+2)        |Masking-key, if MASK set to 1  |
 *      +-------------------------------+-------------------------------+
 *      | Masking-key (continued)       |          Payload Data         |
 *      +-------------------------------- - - - - - - - - - - - - - - - +
 *      :                     Payload Data continued ...                :
 *      + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
 *      |                     Payload Data continued ...                |
 *      +---------------------------------------------------------------+
 *
 *   ref: https://tools.ietf.org/html/rfc6455
 * </pre>
 */
public final class WebSocketCodec extends ProtocolCodec {

    public static final int         HEADER_LENGTH      = 2;
    public static final int         MAX_HEADER_LENGTH  = 10;
    public static final int         MAX_UNSIGNED_SHORT = 0xffff;
    public static final IOException OVER_LIMIT         = EXCEPTION("over writeIndex");
    public static final IOException ILLEGAL_PROTOCOL   = EXCEPTION("illegal protocol");
    public static final String      PROTOCOL_ID        = "WebSocket";
    public static final byte        TYPE_BINARY        = 2;
    public static final byte        TYPE_CLOSE         = 8;
    public static final byte        TYPE_CONTINUE      = 0;
    public static final byte        TYPE_PING          = 9;
    public static final byte        TYPE_PONG          = 10;
    public static final byte        TYPE_TEXT          = 1;
    public static final byte        FIN_EOF            = (byte) (1 << 7);

    static final ByteBuf PING;
    static final ByteBuf PONG;

    static {
        byte o_h0 = (byte) (1 << 7);
        byte o_h1 = (byte) (0 << 7);

        PING = ByteBuf.buffer(2);
        PONG = ByteBuf.buffer(2);
        PING.writeByte((byte) (o_h0 | TYPE_PING));
        PING.writeByte((byte) (o_h1 | 0));
        PONG.writeByte((byte) (o_h0 | TYPE_PONG));
        PONG.writeByte((byte) (o_h1 | 0));
    }

    private final int limit;

    public WebSocketCodec() {
        this(1024 * 64);
    }


    public WebSocketCodec(int limit) {
        this.limit = limit;
    }

    @Override
    public Frame decode(Channel ch, ByteBuf src) throws IOException {
        if (src.readableBytes() < HEADER_LENGTH) {
            return null;
        }
        byte    b0       = src.readByte();
        byte    b1       = src.readByte();
        int     skip     = 2;
        int     mask_len = 0;
        boolean has_mask = (b1 & 0b10000000) != 0;
        if (has_mask) {
            mask_len = 4;
        }
        int payload_len = (b1 & 0x7f);
        if (payload_len < 126) {

        } else if (payload_len == 126) {
            if (src.readableBytes() < 2) {
                src.skipRead(-skip);
                return null;
            }
            skip += 2;
            payload_len = src.readUnsignedShort();
        } else {
            if (src.readableBytes() < 8) {
                src.skipRead(-skip);
                return null;
            }
            skip += 8;
            payload_len = (int) src.readLong();
            if (payload_len < 0) {
                throw OVER_LIMIT;
            }
        }
        if (payload_len > limit) {
            throw OVER_LIMIT;
        }
        if (src.readableBytes() < payload_len + mask_len) {
            src.skipRead(-skip);
            return null;
        }
        byte opcode = (byte) (b0 & 0xF);
        if (((((1 << TYPE_PING) | (1 << TYPE_PONG)) >> opcode) & 1) == 1) {
            if (opcode == TYPE_PING) {
                log_ping_from(ch);
                flush_pong(ch, PONG.duplicate());
            } else {
                log_pong_from(ch);
            }
            return null;
        }
        byte[] array = new byte[payload_len];
        if (has_mask) {
            byte m0 = src.readByte();
            byte m1 = src.readByte();
            byte m2 = src.readByte();
            byte m3 = src.readByte();
            src.readBytes(array);
            int len      = array.length;
            int neat_len = (len >>> 2) << 2;
            for (int i = 0; i < neat_len; i += 4) {
                array[i + 0] ^= m0;
                array[i + 1] ^= m1;
                array[i + 2] ^= m2;
                array[i + 3] ^= m3;
            }
            if (neat_len < len) {
                int remain = len - neat_len;
                if (remain == 1) {
                    array[neat_len + 0] ^= m0;
                } else if (remain == 2) {
                    array[neat_len + 0] ^= m0;
                    array[neat_len + 1] ^= m1;
                } else {
                    array[neat_len + 0] ^= m0;
                    array[neat_len + 1] ^= m1;
                    array[neat_len + 2] ^= m2;
                }
            }
        } else {
            src.readBytes(array);
        }
        WebSocketFrame f = new WebSocketFrame(opcode);
        if (opcode == TYPE_TEXT) {
            f.setContent(new String(array, ch.getCharset()));
        } else if (opcode == TYPE_BINARY) {
            f.setContent(array);
        }
        return f;
    }

    @Override
    public ByteBuf encode(Channel ch, Frame frame) throws IOException {
        WebSocketFrame f   = (WebSocketFrame) frame;
        ByteBuf        buf = f.getBufContent();
        if (buf == null) {
            throw new IOException("null buf content");
        }
        int  size      = buf.writeIndex() - MAX_HEADER_LENGTH;
        byte mark_code = f.getMarkCode();
        if (size < 126) {
            buf.readIndex(8);
            buf.setByte(8, mark_code);
            buf.setByte(9, (byte) size);
        } else if (size <= MAX_UNSIGNED_SHORT) {
            buf.readIndex(6);
            buf.setByte(6, mark_code);
            buf.setByte(7, (byte) 126);
            buf.setShort(8, size);
        } else {
            buf.writeByte(mark_code);
            buf.writeByte((byte) 127);
            buf.writeLong(size);
        }
        return buf;
    }

    @Override
    public String getProtocolId() {
        return PROTOCOL_ID;
    }

    @Override
    public int getHeaderLength() {
        return MAX_HEADER_LENGTH;
    }

    @Override
    protected ByteBuf getPingBuf() {
        return PING.duplicate();
    }

}
