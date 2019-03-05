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
package com.firenio.baseio.codec.http11;

import java.io.IOException;
import java.util.List;

import com.firenio.baseio.buffer.ByteBuf;
import com.firenio.baseio.component.Frame;
import com.firenio.baseio.component.NioEventLoop;
import com.firenio.baseio.component.Channel;
import com.firenio.baseio.component.ProtocolCodec;

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
*
*/
public class WebSocketCodec extends ProtocolCodec {

    public static final String      FRAME_STACK_KEY    = "FRAME_WS_STACK_KEY";
    public static final int         HEADER_LENGTH      = 2;
    public static final int         MAX_HEADER_LENGTH  = 10;
    public static final int         MAX_UNSIGNED_SHORT = 0xffff;
    public static final IOException OVER_LIMIT         = EXCEPTION("over limit");
    public static final String      PROTOCOL_ID        = "WebSocket";
    public static final byte        TYPE_BINARY        = 2;
    public static final byte        TYPE_CLOSE         = 8;
    public static final byte        TYPE_CONTINUE      = 0;
    public static final byte        TYPE_PING          = 9;
    public static final byte        TYPE_PONG          = 10;
    public static final byte        TYPE_TEXT          = 1;

    private final int               frameStackSize;
    private final int               limit;

    public WebSocketCodec() {
        this(1024 * 64);
    }

    public WebSocketCodec(int limit) {
        this(limit, 0);
    }

    public WebSocketCodec(int limit, int frameStackSize) {
        this.limit = limit;
        this.frameStackSize = frameStackSize;
    }

    @Override
    public Frame decode(Channel ch, ByteBuf src) throws IOException {
        if (src.remaining() < HEADER_LENGTH) {
            return null;
        }
        src.markP();
        byte b0 = src.getByte();
        byte b1 = src.getByte();
        int dataLen = 0;
        boolean hasMask = (b1 & 0b10000000) > 0;
        if (hasMask) {
            dataLen += 4;
        }
        int payloadLen = (b1 & 0x7f);
        if (payloadLen < 126) {

        } else if (payloadLen == 126) {
            dataLen += 2;
            if (src.remaining() < dataLen) {
                src.resetP();
                return null;
            }
            payloadLen = src.getUnsignedShort();
        } else {
            dataLen += 8;
            if (src.remaining() < dataLen) {
                src.resetP();
                return null;
            }
            payloadLen = (int) src.getLong();
            if (payloadLen < 0) {
                throw OVER_LIMIT;
            }
        }
        if (payloadLen > limit) {
            throw OVER_LIMIT;
        }
        if (src.remaining() < payloadLen) {
            src.resetP();
            return null;
        }
        boolean eof = (b0 & 0b10000000) > 0;
        byte type = (byte) (b0 & 0xF);
        if (type == TYPE_PING) {
            return newWebSocketFrame(ch).setPing();
        } else if (type == TYPE_PONG) {
            return newWebSocketFrame(ch).setPong();
        }
        byte[] array = new byte[payloadLen];
        if (hasMask) {
            byte m0 = src.getByte();
            byte m1 = src.getByte();
            byte m2 = src.getByte();
            byte m3 = src.getByte();
            src.get(array);
            int length = array.length;
            int len = (length / 4) * 4;
            for (int i = 0; i < len; i += 4) {
                array[i + 0] ^= m0;
                array[i + 1] ^= m1;
                array[i + 2] ^= m2;
                array[i + 3] ^= m3;
            }
            if (len < length) {
                int i = len;
                int remain = length - len;
                if (remain == 1) {
                    array[i + 0] ^= m0;
                } else if (remain == 2) {
                    array[i + 0] ^= m0;
                    array[i + 1] ^= m1;
                } else {
                    array[i + 0] ^= m0;
                    array[i + 1] ^= m1;
                    array[i + 2] ^= m2;
                }
            }
        } else {
            src.get(array);
        }
        WebSocketFrame f = newWebSocketFrame(ch);
        f.setEof(eof);
        f.setType(type);
        if (type == TYPE_TEXT) {
            f.setContent(new String(array, ch.getCharset()));
        } else if (type == TYPE_BINARY) {
            f.setContent(array);
        }
        return f;
    }

    @Override
    public ByteBuf encode(Channel ch, Frame frame) throws IOException {
        WebSocketFrame f = (WebSocketFrame) frame;
        ByteBuf buf = f.getBufContent();
        if (buf != null) {
            buf.flip();
        } else {
            buf = ch.allocate().limit(MAX_HEADER_LENGTH);
        }
        int size = buf.limit() - 10;
        byte header0 = (byte) (0x8f & (f.getType() | 0xf0));
        if (size < 126) {
            buf.position(8);
            buf.putByte(8, header0);
            buf.putByte(9, (byte) size);
        } else if (size <= MAX_UNSIGNED_SHORT) {
            buf.position(6);
            buf.putByte(6, header0);
            buf.putByte(7, (byte) 126);
            buf.putUnsignedShort(8, size);
        } else {
            buf.putByte(header0);
            buf.putByte((byte) 127);
            buf.putLong(size);
        }
        return buf;
    }

    public int getFrameStackSize() {
        return frameStackSize;
    }

    @Override
    public String getProtocolId() {
        return PROTOCOL_ID;
    }

    @Override
    public int headerLength() {
        return MAX_HEADER_LENGTH;
    }

    private WebSocketFrame newWebSocketFrame(Channel ch) {
        if (frameStackSize > 0) {
            //            NioEventLoop eventLoop = ch.getEventLoop();
            //            FixedThreadStack<WebSocketFrame> stack = (FixedThreadStack<WebSocketFrame>) eventLoop
            //                    .getAttribute(FRAME_STACK_KEY);
            //            if (stack == null) {
            //                stack = new FixedThreadStack<>(frameStackSize);
            //                eventLoop.setAttribute(FRAME_STACK_KEY, stack);
            //            }
            //            WebSocketFrame frame = stack.pop();
            //            if (frame == null) {
            //                return new WebSocketFrame(ch, limit);
            //            }
            //            return frame.reset(ch, limit);
        }
        return new WebSocketFrame();
    }

    @Override
    public Frame ping(Channel ch) {
        return new WebSocketFrame().setPing();
    }

    @Override
    public Frame pong(Channel ch, Frame ping) {
        return ping.setPong();
    }

    @SuppressWarnings("unchecked")
    public void release(NioEventLoop eventLoop, Frame frame) {
        //FIXME ..final statck is null or not null
        List<WebSocketFrame> stack = (List<WebSocketFrame>) eventLoop.getAttribute(FRAME_STACK_KEY);
        if (stack != null && stack.size() < frameStackSize) {
            stack.add((WebSocketFrame) frame);
        }
    }

}
