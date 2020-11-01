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
package com.firenio.codec.protobase;

import java.io.IOException;

import com.firenio.buffer.ByteBuf;
import com.firenio.component.Channel;
import com.firenio.component.Frame;
import com.firenio.component.ProtocolCodec;

/**
 * <pre>
 *  B0(type)      : 0-3 : 0:ping, 1:pong, 2:text, 3:binary
 *  B0(position)  : 4-7 : 4:last, 5:broadcast
 *  B1-B3         : FrameLen
 *  B4-B11        : FrameId
 *  B12-B19       : ChannelId
 *
 * </pre>
 */
public final class ProtobaseCodec extends ProtocolCodec {

    public static final IOException ILLEGAL_PROTOCOL = EXCEPTION("illegal protocol");
    public static final IOException OVER_LIMIT       = EXCEPTION("over writeIndex");
    static final        ByteBuf     PING;
    static final        ByteBuf     PONG;
    static final        byte        TYPE_PING        = 0;
    static final        byte        TYPE_PONG        = 1;
    static final        byte        TYPE_TEXT        = 2;
    static final        byte        TYPE_BINARY      = 3;
    static final        byte        LAST             = 1 << 0;
    static final        byte        BROADCAST        = 1 << 1;
    static final        int         PROTOCOL_HEADER  = 4 + 8 + 8;

    static {
        PING = ByteBuf.buffer(4);
        PONG = ByteBuf.buffer(4);
        PING.writeInt(make_type(TYPE_PING) << 24);
        PONG.writeInt(make_type(TYPE_PONG) << 24);
    }

    private final int limit;

    public ProtobaseCodec() {
        this(1024 * 64);
    }

    public ProtobaseCodec(int limit) {
        this.limit = limit;
    }

    @Override
    public Frame decode(Channel ch, ByteBuf src) throws IOException {
        if (src.readableBytes() < 4) {
            return null;
        }
        byte flags = src.getByteAbs(src.absReadIndex());
        int  len   = src.readInt() & 0xffffff;
        int  type  = get_type(flags);
        if (type <= TYPE_PONG) {
            return decode_ping_pong(ch, type);
        }
        if (len > limit) {
            throw OVER_LIMIT;
        }
        if (len > src.readableBytes()) {
            src.skipRead(-4);
            return null;
        }
        long    channelId = src.readLongLE();
        long    frameId   = src.readLongLE();
        byte[] data      = new byte[len - 16];
        src.readBytes(data);
        ProtobaseFrame f = new ProtobaseFrame();
        f.setFlags(flags);
        f.setChannelId(channelId);
        f.setFrameId(frameId);
        if (f.isText()) {
            f.setContent(new String(data, ch.getCharset()));
        } else {
            f.setContent(data);
        }
        return f;
    }

    private Frame decode_ping_pong(Channel ch, int type) throws IOException {
        if (type == TYPE_PING) {
            log_ping_from(ch);
            flush_pong(ch, PONG.duplicate());
        } else if (type == TYPE_PONG) {
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
    protected void encode(Channel ch, Frame frame, ByteBuf buf) {
        ProtobaseFrame f = (ProtobaseFrame) frame;
        buf.setInt(0, (buf.writeIndex() - 4) | (f.getFlags() << 24));
        buf.setLongLE(4, f.getChannelId());
        buf.setLongLE(12, f.getFrameId());
    }

    public int getLimit() {
        return limit;
    }

    @Override
    public String getProtocolId() {
        return "Protobase";
    }

    @Override
    public int getHeaderLength() {
        return PROTOCOL_HEADER;
    }

    static byte make_type(byte type) {
        return (byte) (type << 4);
    }

    static byte get_type(byte flags) {
        return (byte) (flags >> 4);
    }

    static boolean is_ping(byte flags) {
        return TYPE_PING == get_type(flags);
    }

}
