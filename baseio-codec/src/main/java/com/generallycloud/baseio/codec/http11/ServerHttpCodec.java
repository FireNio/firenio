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
import java.util.List;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.ByteBufAllocator;
import com.generallycloud.baseio.component.ByteArrayOutputStream;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.protocol.Future;

/**
 * @author wangkai
 *
 */
public class ServerHttpCodec extends AbstractHttpCodec {

    public static final String  FUTURE_STACK_KEY         = "FixedThreadStack_ServerHttpFuture";
    private static final byte[] PROTOCOL                 = "HTTP/1.1 ".getBytes();
    private static final byte[] SERVER_CL                = "\r\nServer: baseio/0.0.1\r\nContent-Length: "
            .getBytes();
    private static final byte[] SET_COOKIE               = "Set-Cookie:".getBytes();
    private int                 bodyLimit                = 1024 * 512;
    private int                 headerLimit              = 1024 * 8;
    private int                 websocketLimit           = 1024 * 128;
    private final int           httpFutureStackSize;
    private int                 websocketFutureStackSize = 0;

    public ServerHttpCodec() {
        this.httpFutureStackSize = 0;
    }

    public ServerHttpCodec(int headerLimit, int bodyLimit) {
        this.headerLimit = headerLimit;
        this.bodyLimit = bodyLimit;
        this.httpFutureStackSize = 0;
    }

    public ServerHttpCodec(int headerLimit, int bodyLimit, int httpFutureStackSize) {
        this.headerLimit = headerLimit;
        this.bodyLimit = bodyLimit;
        this.httpFutureStackSize = httpFutureStackSize;
    }

    @Override
    public Future createPINGPacket(NioSocketChannel channel) {
        return null;
    }

    @Override
    public Future createPONGPacket(NioSocketChannel channel, Future ping) {
        return null;
    }

    @Override
    public Future decode(NioSocketChannel channel, ByteBuf buffer) throws IOException {
        if (httpFutureStackSize > 0) {
            //            NioEventLoop eventLoop = channel.getEventLoop();
            //            FixedThreadStack<ServerHttpFuture> stack = (FixedThreadStack<ServerHttpFuture>) eventLoop
            //                    .getAttribute(FUTURE_STACK_KEY);
            //            if (stack == null) {
            //                stack = new FixedThreadStack<>(httpFutureStackSize);
            //                eventLoop.setAttribute(FUTURE_STACK_KEY, stack);
            //            }
            //            ServerHttpFuture future = stack.pop();
            //            if (future == null) {
            //                return new ServerHttpFuture(channel, headerLimit, bodyLimit);
            //            }
            //            return future.reset(channel, headerLimit, bodyLimit);
        }
        return new ServerHttpFuture(channel.getContext(), headerLimit, bodyLimit);
    }

    private ByteBuf encode(ByteBufAllocator allocator, ServerHttpFuture f, int length, byte[] array)
            throws IOException {
        ByteBuf buf = allocator.allocate(256);
        try {
            buf.put(PROTOCOL);
            buf.put(f.getStatus().getHeaderBinary());
            buf.put(SERVER_CL);
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
            writeBuf(buf, R);
            writeBuf(buf, N);
            if (length != 0) {
                writeBuf(buf, array, 0, length);
            }
        } catch (Exception e) {
            buf.release();
            throw e;
        }
        return buf.flip();
    }

    @Override
    public ByteBuf encode(NioSocketChannel channel, Future readFuture) throws IOException {
        ByteBufAllocator allocator = channel.allocator();
        ServerHttpFuture f = (ServerHttpFuture) readFuture;
        if (f.isUpdateWebSocketProtocol()) {
            channel.setProtocolCodec(WebSocketCodec.WS_PROTOCOL_CODEC);
            channel.setAttribute(WebSocketFuture.CHANNEL_KEY_SERVICE_NAME, f.getFutureName());
        }
        f.setResponseHeader("Date",
                HttpHeaderDateFormat.getFormat().format(System.currentTimeMillis()));
        ByteArrayOutputStream os = f.getBinaryBuffer();
        if (os != null) {
            return encode(allocator, f, os.size(), os.array());
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
        WebSocketCodec.init(context, websocketLimit, websocketFutureStackSize);
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

    public int getHttpFutureStackSize() {
        return httpFutureStackSize;
    }

    public int getWebsocketFutureStackSize() {
        return websocketFutureStackSize;
    }

    public void setWebsocketLimit(int websocketLimit) {
        this.websocketLimit = websocketLimit;
    }

    public void setWebsocketFutureStackSize(int websocketFutureStackSize) {
        this.websocketFutureStackSize = websocketFutureStackSize;
    }

}
