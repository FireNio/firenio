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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.ByteBufAllocator;
import com.generallycloud.baseio.common.DateUtil;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.NioEventLoop;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.protocol.Frame;

/**
 * @author wangkai
 *
 */
public class ServerHttpCodec extends AbstractHttpCodec {

    public static final String  FRAME_STACK_KEY         = "FixedThreadStack_ServerHttpFrame";
    private static final byte[] PROTOCOL                = "HTTP/1.1 ".getBytes();
    private static final byte[] CONTENT_LENGTH          = "\r\nContent-Length: ".getBytes();
    private static final byte[] SET_COOKIE              = "Set-Cookie:".getBytes();
    private int                 bodyLimit               = 1024 * 512;
    private int                 headerLimit             = 1024 * 8;
    private int                 websocketLimit          = 1024 * 128;
    private final int           httpFrameStackSize;
    private int                 websocketFrameStackSize = 0;
    //    private static final ThreadLocal<HttpDateBytesHolder> dateBytes = new ThreadLocal<>();

    public ServerHttpCodec() {
        this.httpFrameStackSize = 0;
    }

    public ServerHttpCodec(int headerLimit, int bodyLimit) {
        this.headerLimit = headerLimit;
        this.bodyLimit = bodyLimit;
        this.httpFrameStackSize = 0;
    }

    public ServerHttpCodec(int headerLimit, int bodyLimit, int httpFrameStackSize) {
        this.headerLimit = headerLimit;
        this.bodyLimit = bodyLimit;
        this.httpFrameStackSize = httpFrameStackSize;
    }

    public ServerHttpCodec(int httpFrameStackSize) {
        this.httpFrameStackSize = httpFrameStackSize;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Frame decode(NioSocketChannel ch, ByteBuf buffer) throws IOException {
        if (httpFrameStackSize > 0) {
            NioEventLoop eventLoop = ch.getEventLoop();
            List<ServerHttpFrame> stack = (List<ServerHttpFrame>) eventLoop
                    .getAttribute(FRAME_STACK_KEY);
            if (stack == null) {
                stack = new ArrayList<>(httpFrameStackSize);
                eventLoop.setAttribute(FRAME_STACK_KEY, stack);
            }
            if (stack.isEmpty()) {
                return new ServerHttpFrame(ch.getContext(), headerLimit, bodyLimit);
            } else {
                ServerHttpFrame frame = stack.remove(stack.size() - 1);
                return frame.reset(ch);
            }
        }
        return new ServerHttpFrame(ch.getContext(), headerLimit, bodyLimit);
    }

    private ByteBuf encode(ByteBufAllocator allocator, ServerHttpFrame f, int length, byte[] array)
            throws IOException {
        ByteBuf buf = allocator.allocate(512);
        try {
            buf.put(PROTOCOL);
            buf.put(f.getStatus().getBinary());
            buf.put(CONTENT_LENGTH);
            buf.put(String.valueOf(length).getBytes());
            buf.putByte(R);
            buf.putByte(N);
            writeHeaders(f.getResponseHeaders(), buf);
            List<Cookie> cookieList = f.getCookieList();
            if (cookieList != null) {
                for (Cookie c : cookieList) {
                    writeBuf(buf, SET_COOKIE);
                    writeBuf(buf, c.toString().getBytes());
                    writeBuf(buf, R);
                    writeBuf(buf, N);
                }
            }
            int len = 2 + length;
            if (buf.remaining() < len) {
                buf.reallocate(buf.position() + len, true);
            }
            buf.putByte(R);
            buf.putByte(N);
            if (length != 0) {
                buf.put(array, 0, length);
            }
        } catch (Exception e) {
            buf.release();
            throw e;
        }
        return buf.flip();
    }

    private void writeHeaders(Map<byte[], byte[]> headers, ByteBuf buf) {
        if (headers == null) {
            return;
        }
        int len = 0;
        for (Entry<byte[], byte[]> header : headers.entrySet()) {
            byte[] k = header.getKey();
            byte[] v = header.getValue();
            if (v == null) {
                continue;
            }
            len += 4;
            len += k.length;
            len += v.length;
        }
        if (buf.remaining() < len) {
            buf.reallocate(buf.position() + len, true);
        }
        for (Entry<byte[], byte[]> header : headers.entrySet()) {
            byte[] k = header.getKey();
            byte[] v = header.getValue();
            if (v == null) {
                continue;
            }
            buf.put(k);
            buf.putByte(COLON);
            buf.putByte(SPACE);
            buf.put(v);
            buf.putByte(R);
            buf.putByte(N);
        }
    }

    private byte[] getHttpDateBytes() {
        return DateUtil.get().formatHttpBytes();
        //        HttpDateBytesHolder h = dateBytes.get();
        //        if (h == null) {
        //            h = new HttpDateBytesHolder();
        //            dateBytes.set(h);
        //        }
        //        long now = System.currentTimeMillis();
        //        if ((now >> 10) != h.time) {
        //            h.time = now >> 10;
        //            h.value = DateUtil.get().formatHttpBytes(now);
        //        }
        //        return h.value;
    }

    @Override
    public ByteBuf encode(NioSocketChannel ch, Frame readFrame) throws IOException {
        ByteBufAllocator allocator = ch.alloc();
        ServerHttpFrame f = (ServerHttpFrame) readFrame;
        if (f.isUpdateWebSocketProtocol()) {
            ch.setCodec(WebSocketCodec.WS_PROTOCOL_CODEC);
            ch.setAttribute(WebSocketFrame.CHANNEL_KEY_SERVICE_NAME, f.getFrameName());
        }
        f.setResponseHeader(HttpHeader.Date_Bytes, getHttpDateBytes());
        byte[] writeBinary = f.getWriteBinary();
        if (writeBinary != null) {
            return encode(ch.alloc(), f, f.getWriteBinarySize(), writeBinary);
        }
        int writeSize = f.getWriteSize();
        if (writeSize == 0) {
            return encode(allocator, f, 0, null);
        }
        return encode(allocator, f, writeSize, f.getWriteBuffer());
    }

    @Override
    public String getProtocolId() {
        return "HTTP1.1";
    }

    @Override
    public void initialize(ChannelContext context) {
        WebSocketCodec.init(context, websocketLimit, websocketFrameStackSize);
    }

    public int getBodyLimit() {
        return bodyLimit;
    }

    public int getHeaderLimit() {
        return headerLimit;
    }

    public int getWebsocketLimit() {
        return websocketLimit;
    }

    public int getHttpFrameStackSize() {
        return httpFrameStackSize;
    }

    public int getWebsocketFrameStackSize() {
        return websocketFrameStackSize;
    }

    public void setWebsocketLimit(int websocketLimit) {
        this.websocketLimit = websocketLimit;
    }

    public void setWebsocketFrameStackSize(int websocketFrameStackSize) {
        this.websocketFrameStackSize = websocketFrameStackSize;
    }

    class HttpDateBytesHolder {
        long   time;
        byte[] value;
    }

}
