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
package sample.baseio.http11.proxy4cloud;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

import com.firenio.baseio.buffer.ByteBuf;
import com.firenio.baseio.common.Util;
import com.firenio.baseio.component.ChannelAcceptor;
import com.firenio.baseio.component.ChannelActiveIdleEventListener;
import com.firenio.baseio.component.ChannelAliveIdleEventListener;
import com.firenio.baseio.component.ChannelConnector;
import com.firenio.baseio.component.Frame;
import com.firenio.baseio.component.LoggerChannelOpenListener;
import com.firenio.baseio.component.NioEventLoop;
import com.firenio.baseio.component.NioEventLoopGroup;
import com.firenio.baseio.component.Channel;
import com.firenio.baseio.component.ProtocolCodec;

import sample.baseio.http11.proxy4cloud.HttpProxy4CloudServer.ProxySession4Cloud;
import sample.baseio.http11.service.CountChannelListener;

/**
 * @author wangkai
 *
 */
public class NetDataTransferServer {

    private static final NetDataTransferServer instance          = new NetDataTransferServer();
    private AtomicInteger                      connectorCallback = new AtomicInteger();

    public static NetDataTransferServer get() {
        return instance;
    }

    public static void mask(ByteBuf src, byte m) {
        ByteBuffer buf = src.nioBuffer();
        int p = buf.position();
        int l = buf.limit();
        for (; p < l; p++) {
            buf.put(p, (byte) (buf.get(p) ^ m));
        }
    }

    public synchronized void startup(NioEventLoopGroup group, int port) throws Exception {

        ChannelAcceptor context = new ChannelAcceptor(group, port);
        context.addProtocolCodec(new NetDataTransfer());
        context.addChannelIdleEventListener(new ChannelAliveIdleEventListener());
        context.addChannelEventListener(new LoggerChannelOpenListener());
        context.addChannelEventListener(new CountChannelListener());
        context.bind();
    }

    public static void main(String[] args) throws Exception {

        get().startup(new NioEventLoopGroup(true), 18088);

    }

    class NetDataTransfer extends ProtocolCodec {

        @Override
        public String getProtocolId() {
            return "NetDataTransfer";
        }

        @Override
        public int headerLength() {
            return 0;
        }

        @Override
        public ByteBuf encode(Channel ch, Frame frame) throws IOException {
            return null;
        }

        @Override
        public Frame decode(Channel ch_src, ByteBuf src) throws Exception {
            ProxySession4Cloud s = ProxySession4Cloud.get(ch_src);
            if (s.handshakeFinished) {
                if (s.connector == null || !s.connector.isConnected()) {
                    NioEventLoop el = ch_src.getEventLoop();
                    ChannelConnector context = new ChannelConnector(el, s.host, s.port);
                    context.addProtocolCodec(new HttpProxyConnect(ch_src, s.mask));
                    context.setPrintConfig(false);
                    context.addChannelIdleEventListener(new ChannelActiveIdleEventListener());
                    context.addChannelEventListener(new LoggerChannelOpenListener());
                    context.addChannelEventListener(new CountChannelListener());
                    ByteBuf buf = ch_src.alloc().allocate(src.remaining());
                    buf.put(src);
                    buf.flip();
                    connectorCallback.getAndIncrement();
                    s.connector = context;
                    s.connector.connect((ch, ex) -> {
                        connectorCallback.getAndDecrement();
                        if (ex == null) {
                            mask(buf, s.mask);
                            ch.writeAndFlush(buf);
                        } else {
                            buf.release();
                            ProxySession4Cloud.remove(ch_src);
                            Util.close(ch_src);
                        }
                    }, 10000);
                } else {
                    ByteBuf buf = ch_src.alloc().allocate(src.remaining());
                    buf.put(src);
                    buf.flip();
                    mask(buf, s.mask);
                    s.connector.getChannel().writeAndFlush(buf);
                }
            } else {
                short hostLen = src.getUnsignedByte(0);
                if (src.remaining() < hostLen + 5) {
                    return null;
                }
                byte b0 = src.getByte(1);
                byte b1 = src.getByte(2);
                if (!(b0 == 83 && b1 == 38)) {
                    ch_src.close();
                    return null;
                }
                byte[] hostBytes = new byte[hostLen];
                int port = src.getUnsignedShort(3);
                src.skip(5);
                src.get(hostBytes);
                String host = new String(hostBytes);
                s.mask = (byte) hostLen;
                s.host = host;
                s.port = port;
                s.handshakeFinished = true;
                return decode(ch_src, src);
            }
            return null;
        }

    }

    class HttpProxyConnect extends ProtocolCodec {

        Channel ch_src;
        byte    mask;

        public HttpProxyConnect(Channel ch_src, byte mask) {
            this.ch_src = ch_src;
            this.mask = mask;
        }

        @Override
        public String getProtocolId() {
            return "HttpProxyConnect";
        }

        @Override
        public ByteBuf encode(Channel ch, Frame frame) throws IOException {
            return null;
        }

        @Override
        public int headerLength() {
            return 0;
        }

        @Override
        public Frame decode(Channel ch, ByteBuf src) throws IOException {
            ByteBuf buf = ch_src.alloc().allocate(src.remaining());
            buf.put(src);
            buf.flip();
            mask(buf, mask);
            ch_src.writeAndFlush(buf);
            return null;
        }

    }

}
