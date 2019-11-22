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
package sample.http11.proxy4cloud;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import sample.http11.proxy.HttpProxyServer.HttpProxyAttr;

import com.firenio.buffer.ByteBuf;
import com.firenio.codec.http11.HttpAttachment;
import com.firenio.codec.http11.HttpCodec;
import com.firenio.codec.http11.HttpFrame;
import com.firenio.codec.http11.HttpMethod;
import com.firenio.codec.http11.HttpStatus;
import com.firenio.collection.DelayedQueue.DelayTask;
import com.firenio.common.Util;
import com.firenio.component.Channel;
import com.firenio.component.ChannelAcceptor;
import com.firenio.component.ChannelConnector;
import com.firenio.component.ChannelEventListenerAdapter;
import com.firenio.component.Frame;
import com.firenio.component.LoggerChannelOpenListener;
import com.firenio.component.NioEventLoop;
import com.firenio.component.NioEventLoopGroup;
import com.firenio.component.ProtocolCodec;
import com.firenio.log.DebugUtil;

public class HttpProxy4CloudServer {

    static final String                CONNECT_RES     = "HTTP/1.1 200 Connection Established\r\n\r\n";
    static final ByteBuf               CONNECT_RES_BUF = ByteBuf.wrapAuto(CONNECT_RES.getBytes());
    static final String                netHost;
    static final int                   netPort         = 18088;
    static final HttpProxy4CloudServer server          = new HttpProxy4CloudServer();

    static {
        String host;
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("127.0.0.1", netPort), 50);
            Util.close(socket);
            host = "127.0.0.1";
        } catch (IOException e) {
            host = "47.52.62.51";
        }
        netHost = host;
        DebugUtil.debug("remote host: " + netHost);
    }

    private ChannelAcceptor context;

    private volatile boolean enable = true;

    public static HttpProxy4CloudServer get() {
        return server;
    }

    public static void main(String[] args) throws Exception {

        get().strtup(new NioEventLoopGroup(true), 8088);

    }

    public void disable() {
        enable = false;
    }

    public void enable() {
        enable = true;
    }

    public synchronized void stop() {
        Util.unbind(context);
    }

    public synchronized void strtup(NioEventLoopGroup serverG, int port) throws Exception {
        if (context != null && context.isActive()) {
            return;
        }
        context = new ChannelAcceptor(serverG, 8088);
        context.addProtocolCodec(new HttpProxy4CloudCodec());
        context.addChannelEventListener(new LoggerChannelOpenListener());
        context.addChannelEventListener(new ChannelEventListenerAdapter() {
            @Override
            public void channelClosed(Channel ch) {
                Util.close(((HttpProxy4CloudAttr) ch.getAttachment()).target);
            }
        });
        context.bind();
    }

    class HttpProxy4CloudCodec extends HttpCodec {

        @Override
        public Frame decode(final Channel ch_src, ByteBuf src) throws Exception {
            HttpProxy4CloudAttr attr = (HttpProxy4CloudAttr) ch_src.getAttachment();
            Channel             t    = attr.target;
            if (t == null) {
                Frame frame = super.decode(ch_src, src);
                if (frame != null) {
                    final HttpFrame f = (HttpFrame) frame;
                    if (!enable || f.getMethod() != HttpMethod.CONNECT) {
                        f.setStatus(HttpStatus.C503);
                        f.setString("503 service unavailable", ch_src);
                        ch_src.writeAndFlush(f);
                    } else {
                        int      _port = 443;
                        String[] arr   = f.getHost().split(":");
                        String   host  = arr[0];
                        if (arr.length == 2) {
                            _port = Integer.parseInt(arr[1]);
                        }
                        final int        port    = _port;
                        NioEventLoop     el      = ch_src.getEventLoop();
                        ChannelConnector context = new ChannelConnector(el, netHost, netPort);
                        context.addProtocolCodec(new ProtocolCodec() {

                            @Override
                            public Frame decode(Channel ch, ByteBuf src) {
                                Channel t   = (Channel) ch.getAttachment();
                                ByteBuf buf = t.alloc().allocate(src.readableBytes());
                                buf.writeBytes(src);
                                t.writeAndFlush(buf);
                                return null;
                            }

                            @Override
                            public String getProtocolId() {
                                return "http-proxy-connect";
                            }

                            @Override
                            public int getHeaderLength() {
                                return 0;
                            }
                        });
                        context.setPrintConfig(false);
                        context.addChannelEventListener(new LoggerChannelOpenListener());
                        context.addChannelEventListener(NetDataTransferServer.CLOSE_TARGET);
                        context.connect((ch_target, ex) -> {
                            if (ex == null) {
                                attr.target = ch_target;
                                ch_target.setAttachment(ch_src);
                                byte[]  host_bytes = host.getBytes();
                                int     len        = 5 + host_bytes.length;
                                ByteBuf head       = ch_target.alloc().allocate(len);
                                head.writeByte((byte) host_bytes.length);
                                head.writeByte((byte) 83);
                                head.writeByte((byte) 38);
                                head.writeShort(port);
                                head.writeBytes(host_bytes);
                                ch_target.writeAndFlush(head);
                                el.schedule(new DelayTask(10) {
                                    @Override
                                    public void run() {
                                        ch_src.writeAndFlush(CONNECT_RES_BUF.duplicate());
                                    }
                                });
                            } else {
                                Util.close(ch_src);
                            }
                        });
                    }
                }
                return null;
            } else {
                ByteBuf buf = t.alloc().allocate(src.readableBytes());
                buf.writeBytes(src);
                t.writeAndFlush(buf);
            }
            return null;
        }

        @Override
        protected void parse_line_one(HttpFrame f, CharSequence line) throws IOException {
            if (line.charAt(0) == 'C' && line.charAt(1) == 'O' && line.charAt(2) == 'N' && line.charAt(3) == 'N' && line.charAt(4) == 'E' && line.charAt(5) == 'C' && line.charAt(6) == 'T' && line.charAt(7) == ' ') {
                f.setMethod(HttpMethod.CONNECT);
                parse_url(f, 8, line);
            } else {
                super.parse_line_one(f, line);
            }
        }

        @Override
        protected Object newAttachment() {
            return new HttpProxy4CloudAttr();
        }
    }

    static class HttpProxy4CloudAttr extends HttpAttachment {
        Channel target;
    }

}
