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

import com.firenio.baseio.buffer.ByteBuf;
import com.firenio.baseio.common.Util;
import com.firenio.baseio.component.Channel;
import com.firenio.baseio.component.ChannelAcceptor;
import com.firenio.baseio.component.ChannelActiveListener;
import com.firenio.baseio.component.ChannelAliveListener;
import com.firenio.baseio.component.ChannelConnector;
import com.firenio.baseio.component.Frame;
import com.firenio.baseio.component.LoggerChannelOpenListener;
import com.firenio.baseio.component.NioEventLoop;
import com.firenio.baseio.component.NioEventLoopGroup;
import com.firenio.baseio.component.ProtocolCodec;

import sample.baseio.http11.service.CountChannelListener;

/**
 * @author wangkai
 *
 */
public class NetDataTransferServer {

    private static final NetDataTransferServer instance = new NetDataTransferServer();
    private static final int                   MASK     = 0x12345678;

    public synchronized void startup(NioEventLoopGroup group, int port) throws Exception {

        ChannelAcceptor context = new ChannelAcceptor(group, port);
        context.addProtocolCodec(new NetDataTransfer());
        context.addChannelIdleEventListener(new ChannelAliveListener());
        context.addChannelEventListener(new LoggerChannelOpenListener());
        context.addChannelEventListener(new CountChannelListener());
        context.bind();
    }

    static class NetDataTransfer extends ProtocolCodec {

        @Override
        public Frame decode(Channel ch, ByteBuf src) throws Exception {
            TcpProxyAttar a = (TcpProxyAttar) ch.getAttachment();
            if (a.is_server) {
                if (a.is_handshakeFinished) {
                    if (a.isConnected()) {
                        Channel ch_src = a.getChannel();
                        ByteBuf buf = ch_src.alloc().allocate(src.remaining());
                        buf.put(src);
                        buf.flip();
                        mask(buf);
                        ch_src.writeAndFlush(buf);
                    } else {
                        Util.close(a.connector);
                        NioEventLoop el = ch.getEventLoop();
                        ChannelConnector context = new ChannelConnector(el, a.host, a.port);
                        context.addProtocolCodec(this);
                        context.setPrintConfig(false);
                        context.addChannelIdleEventListener(new ChannelActiveListener());
                        context.addChannelEventListener(new LoggerChannelOpenListener());
                        context.addChannelEventListener(new CountChannelListener());
                        ByteBuf buf = ch.alloc().allocate(src.remaining());
                        buf.put(src);
                        buf.flip();
                        a.connector = context;
                        a.connector.connect((raw_ch, ex) -> {
                            if (ex == null) {
                                TcpProxyAttar attr = new TcpProxyAttar(false);
                                attr.from = ch;
                                raw_ch.setAttachment(attr);
                                mask(buf);
                                raw_ch.writeAndFlush(buf);
                            }else{
                                Util.close(raw_ch);
                                buf.release();
                            }
                        }, 10000);
                    }
                } else {
                    short hostLen = src.getUnsignedByte(0);
                    if (src.remaining() < hostLen + 5) {
                        return null;
                    }
                    byte b0 = src.getByte(1);
                    byte b1 = src.getByte(2);
                    if (!(b0 == 83 && b1 == 38)) {
                        ch.close();
                        return null;
                    }
                    byte[] hostBytes = new byte[hostLen];
                    int port = src.getUnsignedShort(3);
                    src.skip(5);
                    src.get(hostBytes);
                    String host = new String(hostBytes);
                    a.host = host;
                    a.port = port;
                    a.is_handshakeFinished = true;
                    return decode(ch, src);
                }
            } else {
                Channel ch_src = a.from;
                ByteBuf buf = ch_src.alloc().allocate(src.remaining());
                buf.put(src);
                buf.flip();
                mask(buf);
                ch_src.writeAndFlush(buf);
                return null;
            }
            return null;
        }

        @Override
        public ByteBuf encode(Channel ch, Frame frame) throws IOException {
            return null;
        }

        @Override
        public String getProtocolId() {
            return "NetDataTransfer";
        }

        @Override
        public int headerLength() {
            return 0;
        }
        
        @Override
        protected Object newAttachment() {
            return new TcpProxyAttar(true);
        }

    }

    public static class TcpProxyAttar {

        public ChannelConnector connector;
        public Channel          from;
        public final boolean    is_server;
        public boolean          is_handshakeFinished;
        public String           host;
        public int              port;

        public TcpProxyAttar(boolean is_server) {
            this.is_server = is_server;
        }

        public Channel getChannel() {
            return connector.getChannel();
        }

        public boolean isConnected() {
            return connector != null && connector.isConnected();
        }

    }

    public static NetDataTransferServer get() {
        return instance;
    }

    public static void main(String[] args) throws Exception {

        get().startup(new NioEventLoopGroup(true), 18088);

    }

    public static void mask(ByteBuf src) {
//        mask(src, MASK);
    }

    public static void mask(ByteBuf src, int mask) {
        byte m = (byte) mask;
        ByteBuffer buf = src.nioBuffer();
        int p = buf.position();
        int l = buf.limit();
        for (; p < l; p++) {
            buf.put(p, (byte) (buf.get(p) ^ m));
        }
    }

    public static void mask1(ByteBuf src, int mask) {
        ByteBuffer buf = src.nioBuffer();
        int p = buf.position();
        int l = buf.limit();
        byte m1 = (byte) (mask >>> 24);
        byte m2 = (byte) (mask >>> 16);
        byte m3 = (byte) (mask >>> 8);
        byte m4 = (byte) (mask >>> 0);
        int ll = (((l - p) / 4) * 4) + p;
        for (; p < ll; p += 4) {
            buf.put(p + 0, (byte) (buf.get(p + 0) ^ m1));
            buf.put(p + 1, (byte) (buf.get(p + 1) ^ m2));
            buf.put(p + 2, (byte) (buf.get(p + 2) ^ m3));
            buf.put(p + 3, (byte) (buf.get(p + 3) ^ m4));
        }
        if (l > p) {
            int r = l - p;
            if (r == 1) {
                buf.put(p + 0, (byte) (buf.get(p + 0) ^ m1));
            } else if (r == 2) {
                buf.put(p + 0, (byte) (buf.get(p + 0) ^ m1));
                buf.put(p + 1, (byte) (buf.get(p + 1) ^ m2));
            } else {
                buf.put(p + 0, (byte) (buf.get(p + 0) ^ m1));
                buf.put(p + 1, (byte) (buf.get(p + 1) ^ m2));
                buf.put(p + 2, (byte) (buf.get(p + 2) ^ m3));
            }
        }

    }

}
