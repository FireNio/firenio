/*
 * Copyright 2015 The Baseio Project
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
package com.firenio.baseio.codec.protobase;

import java.io.IOException;

import com.firenio.baseio.buffer.ByteBuf;
import com.firenio.baseio.component.Frame;
import com.firenio.baseio.component.Channel;
import com.firenio.baseio.component.ProtocolCodec;

/**
 * <pre>
 *  B0    : 0-1 : 0:PING, 1:PONG,    2:P2P, 3:BRODCAST
 *  B0    : 2   : 0:text, 1:binary
 *  B0    : 3   : 0:last, 1:continue
 *  B0    : 4-7 : ExtType 
 *  B1-B3 : FrameLen
 *  B4-B7 : FrameId
 *  B8-B11: ChannelId
 *  
 * </pre>
 */
public class ProtobaseCodec extends ProtocolCodec {

    public static final IOException ILLEGAL_PROTOCOL = EXCEPTION("illegal protocol");
    public static final IOException OVER_LIMIT       = EXCEPTION("over limit");
    private static final ByteBuf    PING;
    private static final ByteBuf    PONG;
    public static final int         PROTOCOL_HEADER  = 12;
    public static final int         PROTOCOL_PING    = 0;
    public static final int         PROTOCOL_PONG    = 1;

    static {
        PING = ByteBuf.heap(4);
        PONG = ByteBuf.heap(4);
        PING.putInt(0 << 31);
        PONG.putInt(1 << 30);
        PING.flip();
        PONG.flip();
    }

    private final int limit;

    public ProtobaseCodec() {
        this(1024 * 64);
    }

    public ProtobaseCodec(int limit) {
        this.limit = limit;
    }

    private Frame decodePing(int type) throws IOException {
        if (type == PROTOCOL_PING) {
            return newProtobaseFrame().setPing();
        } else if (type == PROTOCOL_PONG) {
            return newProtobaseFrame().setPong();
        } else {
            throw ILLEGAL_PROTOCOL;
        }
    }

    @Override
    public Frame decode(Channel ch, ByteBuf src) throws IOException {
        if (src.remaining() < 4) {
            return null;
        }
        int h1 = src.getByte(0) & 0xff;
        int type = h1 >> 6;
        int len = src.getInt() & 0xffffff;
        if (type < 2) {
            return decodePing(type);
        }
        if (len > limit) {
            throw OVER_LIMIT;
        }
        if (len > src.remaining()) {
            src.skip(-4);
            return null;
        }
        boolean broadcast = type == 3;
        boolean text = ((h1 >> 5) & 1) == 0;
        boolean last = ((h1 >> 4) & 1) == 0;
        byte extType = (byte) (h1 & 0xf);
        int frameId = src.getInt();
        int channelId = src.getInt();
        ProtobaseFrame f = newProtobaseFrame();
        f.setBroadcast(broadcast);
        f.setChannelId(channelId);
        f.setFrameId(frameId);
        f.setExtType(extType);
        f.setText(text);
        f.setBroadcast(broadcast);
        f.setLast(last);
        byte[] data = new byte[len];
        src.get(data);
        if (text) {
            f.setContent(new String(data, ch.getCharset()));
        } else {
            f.setContent(data);
        }
        return f;
    }

    @Override
    public ByteBuf encode(Channel ch, Frame frame) throws IOException {
        if (frame.isTyped()) {
            return frame.isPing() ? PING.duplicate() : PONG.duplicate();
        }
        ProtobaseFrame f = (ProtobaseFrame) frame;
        byte h1 = (byte) (f.getExtType() & 0xf);
        if (f.isBroadcast()) {
            h1 |= 0b11000000;
        } else {
            h1 |= 0b10000000;
        }
        if (f.isBinary()) {
            h1 |= 0b00100000;
        }
        if (f.isContinue()) {
            h1 |= 0b00010000;
        }
        ByteBuf buf = (ByteBuf) f.getContent();
        buf.flip();
        int len = buf.limit() - PROTOCOL_HEADER;
        len |= h1 << 24;
        buf.putInt(0, len);
        buf.putInt(4, f.getFrameId());
        buf.putInt(8, f.getChannelId());
        return buf;
    }

    public int getLimit() {
        return limit;
    }

    @Override
    public String getProtocolId() {
        return "Protobase";
    }

    @Override
    public int headerLength() {
        return PROTOCOL_HEADER;
    }

    ProtobaseFrame newProtobaseFrame() {
        return new ProtobaseFrame();
    }

    @Override
    public Frame ping(Channel ch) {
        return new ProtobaseFrame().setPing();
    }

}
