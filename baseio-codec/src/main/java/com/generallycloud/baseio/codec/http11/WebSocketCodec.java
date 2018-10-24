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
package com.generallycloud.baseio.codec.http11;

import java.io.IOException;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.ByteBufAllocator;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.protocol.Frame;
import com.generallycloud.baseio.protocol.ProtocolCodec;

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

    public static final String   FRAME_STACK_KEY    = "FixedThreadStack_WebSocketFrame";
    public static final int      PROTOCOL_HEADER    = 2;
    public static final String   PROTOCOL_ID        = "WebSocket";
    public static final byte     TYPE_BINARY        = 2;
    public static final byte     TYPE_CLOSE         = 8;
    public static final byte     TYPE_PING          = 9;
    public static final byte     TYPE_PONG          = 10;
    public static final byte     TYPE_TEXT          = 1;
    public static final int      MAX_UNSIGNED_SHORT = 0xffff;
    public static WebSocketCodec WS_PROTOCOL_CODEC;

    static void init(ChannelContext context, int limit, int frameStackSize) {
        WS_PROTOCOL_CODEC = new WebSocketCodec(limit, frameStackSize);
        WS_PROTOCOL_CODEC.initialize(context);
    }

    private final int limit;
    private final int frameStackSize;

    public WebSocketCodec(int limit, int frameStackSize) {
        this.limit = limit;
        this.frameStackSize = frameStackSize;
    }

    @Override
    public Frame ping(NioSocketChannel ch) {
        return new WebSocketFrame().setPing();
    }

    @Override
    public Frame pong(NioSocketChannel ch, Frame ping) {
        return ping.setPong();
    }

    @Override
    public Frame decode(NioSocketChannel ch, ByteBuf buffer) throws IOException {
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
        return new WebSocketFrame(ch, limit);
    }

    @Override
    public ByteBuf encode(NioSocketChannel ch, Frame frame) throws IOException {
        ByteBufAllocator allocator = ch.alloc();
        WebSocketFrame f = (WebSocketFrame) frame;
        byte[] data = f.getWriteBuffer();
        int size = f.getWriteSize();
        byte header0 = (byte) (0x8f & (f.getType() | 0xf0));
        ByteBuf buf;
        if (size < 126) {
            buf = allocator.allocate(2 + size);
            buf.putByte(header0);
            buf.putByte((byte) size);
            if (size > 0) {
                buf.put(data, 0, size);
            }
        } else if (size <= MAX_UNSIGNED_SHORT) {
            buf = allocator.allocate(4 + size);
            buf.putByte(header0);
            buf.putByte((byte) 126);
            buf.putUnsignedShort(size);
            buf.put(data, 0, size);
        } else {
            buf = allocator.allocate(10 + size);
            buf.putByte(header0);
            buf.putByte((byte) 127);
            buf.putLong(size);
            buf.put(data, 0, size);
        }
        return buf.flip();
    }

    public int getFrameStackSize() {
        return frameStackSize;
    }

    @Override
    public String getProtocolId() {
        return PROTOCOL_ID;
    }

}
