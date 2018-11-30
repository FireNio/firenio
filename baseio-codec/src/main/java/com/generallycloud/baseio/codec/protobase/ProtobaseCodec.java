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
import com.generallycloud.baseio.buffer.ByteBufUtil;
import com.generallycloud.baseio.common.Util;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.protocol.Frame;
import com.generallycloud.baseio.protocol.ProtocolCodec;

/**
 * <pre>
 *  B0 -B3  : 报文总长度        大于0:普通消息 -1:心跳PING -2:心跳PONG
 *  B4 :0   : 广播类型          0:P2P           1:BRODCAST
 *  B4 :1   : 是否包含FrameId   4 byte   
 *  B4 :2   : 是否包含ChannelId 4 byte
 *  B4 :3   : 是否包含Text      4 byte
 *  B4 :4   : 是否包含Binary    4 byte
 *  B4 :5   : 预留
 *  B4 :6   : 预留
 *  B4 :7   : 预留
 *  B5      : 消息类型FrameType
 *  .....   ：FrameId,ChannelId,Text,Binary
 *  
 * </pre>
 */
public class ProtobaseCodec extends ProtocolCodec {

    public static final int      PROTOCOL_PING = -1;
    public static final int      PROTOCOL_PONG = -2;

    private static final ByteBuf PING;
    private static final ByteBuf PONG;

    static {
        PING = ByteBufUtil.heap(4);
        PONG = ByteBufUtil.heap(4);
        PING.putInt(PROTOCOL_PING);
        PONG.putInt(PROTOCOL_PONG);
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

    @Override
    public Frame ping(NioSocketChannel ch) {
        return new ProtobaseFrame().setPing();
    }
    
    ProtobaseFrame newProtobaseFrame() {
        return new ProtobaseFrame();
    }

    @Override
    public Frame decode(NioSocketChannel ch, ByteBuf src) throws IOException {
        if (src.remaining() < 4) {
            return null;
        }
        int len = src.getInt();
        if (len < 0) {
            if (len == ProtobaseCodec.PROTOCOL_PING) {
                return newProtobaseFrame().setPing();
            } else if (len == ProtobaseCodec.PROTOCOL_PONG) {
                return newProtobaseFrame().setPong();
            } else {
                throw new IOException("illegal length:" + len);
            }
        }
        if (len > limit) {
            throw new IOException("over limit" + len);
        }
        if (len > src.remaining()) {
            src.skip(-4);
            return null;
        }
        ProtobaseFrame f = newProtobaseFrame();
        byte h1 = src.getByte();
        byte frameType = src.getByte();
        boolean isBroadcast = ((h1 & 0b10000000) != 0);
        boolean hasText = ((h1 & 0b00010000) != 0);
        boolean hasBinary = ((h1 & 0b00001000) != 0);
        int frameId = 0;
        int channelId = 0;
        if (((h1 & 0b01000000) != 0)) {
            frameId = src.getInt();
        }
        if (((h1 & 0b00100000) != 0)) {
            channelId = src.getInt();
        }
        int textLen = 0;
        int binaryLen = 0;
        f.setBroadcast(isBroadcast);
        f.setChannelId(channelId);
        f.setFrameId(frameId);
        f.setFrameType(frameType);
        if (hasText) {
            textLen = src.getInt();
            src.markL();
            src.limit(src.position() + textLen);
            f.setReadText(Util.decode(ch.getCharset(), src.nioBuffer()));
            src.reverse();
            src.resetL();
        }
        if (hasBinary) {
            binaryLen = src.getInt();
            src.markL();
            src.limit(src.position() + binaryLen);
            f.setReadBinary(src.getBytes());
            src.reverse();
            src.resetL();
        }
        return f;
    }

    public int getLimit() {
        return limit;
    }

    @Override
    public ByteBuf encode(NioSocketChannel ch, Frame frame) throws IOException {
        if (frame.isTyped()) {
            return frame.isPing() ? PING.duplicate() : PONG.duplicate();
        }
        ProtobaseFrame f = (ProtobaseFrame) frame;
        int allLen = 6;
        int textWriteSize = f.getWriteSize();
        int binaryWriteSize = f.getWriteBinarySize();
        byte h1 = 0b00000000;
        if (f.isBroadcast()) {
            h1 |= 0b10000000;
        }
        if (f.getFrameId() > 0) {
            h1 |= 0b01000000;
            allLen += 4;
        }
        if (f.getChannelId() > 0) {
            h1 |= 0b00100000;
            allLen += 4;
        }
        if (textWriteSize > 0) {
            h1 |= 0b00010000;
            allLen += 4;
            allLen += textWriteSize;
        }
        if (binaryWriteSize > 0) {
            h1 |= 0b00001000;
            allLen += 4;
            allLen += binaryWriteSize;
        }
        ByteBuf buf = ch.alloc().allocate(allLen);
        buf.putInt(allLen - 4);
        buf.putByte(h1);
        buf.putByte(f.getFrameType());
        if (f.getFrameId() > 0) {
            buf.putInt(f.getFrameId());
        }
        if (f.getChannelId() > 0) {
            buf.putInt(f.getChannelId());
        }
        if (textWriteSize > 0) {
            buf.putInt(textWriteSize);
            buf.put(f.getWriteBuffer(), 0, textWriteSize);
        }
        if (binaryWriteSize > 0) {
            buf.putInt(binaryWriteSize);
            buf.put(f.getWriteBinary(), 0, binaryWriteSize);
        }
        return buf.flip();
    }

    @Override
    public String getProtocolId() {
        return "Protobase";
    }

}
