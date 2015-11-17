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
package sample.baseio.http11.proxy;

import java.io.IOException;

import com.firenio.baseio.buffer.ByteBuf;
import com.firenio.baseio.codec.http11.ClientHttpCodec;
import com.firenio.baseio.codec.http11.ClientHttpFrame;
import com.firenio.baseio.codec.http11.HttpCodec;
import com.firenio.baseio.codec.http11.HttpFrame;
import com.firenio.baseio.codec.http11.HttpHeader;
import com.firenio.baseio.codec.http11.HttpMethod;
import com.firenio.baseio.collection.IntEntry;
import com.firenio.baseio.common.Util;
import com.firenio.baseio.component.ChannelAcceptor;
import com.firenio.baseio.component.ChannelConnector;
import com.firenio.baseio.component.Frame;
import com.firenio.baseio.component.IoEventHandle;
import com.firenio.baseio.component.LoggerChannelOpenListener;
import com.firenio.baseio.component.NioEventLoop;
import com.firenio.baseio.component.NioEventLoopGroup;
import com.firenio.baseio.component.Channel;
import com.firenio.baseio.component.ProtocolCodec;

public class HttpProxyServer {

    static final String          CONNECT_RES     = "HTTP/1.1 200 Connection Established\r\n\r\n";
    static final ByteBuf         CONNECT_RES_BUF = ByteBuf.wrap(CONNECT_RES.getBytes());
    static final HttpProxyServer server          = new HttpProxyServer();
    private ChannelAcceptor      context;

    public synchronized void stop() {
        Util.unbind(context);
    }

    public synchronized void strtup(NioEventLoopGroup group, int port) throws Exception {
        if (context != null && context.isActive()) {
            return;
        }

        IoEventHandle eventHandle = new IoEventHandle() {

            @Override
            public void accept(Channel ch_src, Frame frame) throws Exception {
                final HttpFrame f = (HttpFrame) frame;
                if (f.getMethod() == HttpMethod.CONNECT) {
                    ch_src.writeAndFlush(CONNECT_RES_BUF.duplicate());
                    ProxySession s = ProxySession.get(ch_src);
                    String[] arr = f.getHost().split(":");
                    s.host = arr[0];
                    s.port = Integer.parseInt(arr[1]);
                    s.handshakeFinished = true;
                } else {
                    String host = f.getHost();
                    String[] arr = host.split(":");
                    int port = 80;
                    if (arr.length == 2) {
                        port = Integer.parseInt(arr[1]);
                    }
                    if (f.getRequestHeaders().remove(HttpHeader.Proxy_Connection.getId()) == null) {
                        return;
                    }
                    NioEventLoop el = ch_src.getEventLoop();
                    ChannelConnector context = new ChannelConnector(el, arr[0], port);
                    context.addProtocolCodec(new ClientHttpCodec());
                    context.setIoEventHandle(new IoEventHandle() {

                        @Override
                        public void accept(Channel ch, Frame frame) throws Exception {
                            ClientHttpFrame res = (ClientHttpFrame) frame;
                            for (IntEntry<String> header : res.getResponse_headers().entries()) {
                                if (header.value() == null) {
                                    continue;
                                }
                                f.setResponseHeader(header.key(), header.value().getBytes());
                            }
                            f.removeResponseHeader(HttpHeader.Content_Length);
                            if (res.getContent() != null) {
                                f.setContent(res.getContent());
                            } else if (res.isChunked()) {
                                f.removeResponseHeader(HttpHeader.Transfer_Encoding);
                                f.removeResponseHeader(HttpHeader.Content_Encoding);
                                f.setBytes("not support chunked now.".getBytes());

                            }
                            ch_src.writeAndFlush(f);
                            ch.close();
                        }
                    });
                    context.setPrintConfig(false);
                    context.addChannelEventListener(new LoggerChannelOpenListener());
                    context.connect((ch, ex) -> {
                        if (ex == null) {
                            ClientHttpFrame req = new ClientHttpFrame(f.getRequestURL(),
                                    f.getMethod());
                            req.setRequestHeaders(f.getRequestHeaders());
                            req.getRequestHeaders().remove(HttpHeader.Proxy_Connection.getId());
                            if (f.getMethod() == HttpMethod.POST) {
                                req.setContent(f.getContent());
                            }
                            try {
                                ch.writeAndFlush(req);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        };

        context = new ChannelAcceptor(group, 8088);
        context.addProtocolCodec(new HttpProxyCodec());
        context.setIoEventHandle(eventHandle);
        context.addChannelEventListener(new LoggerChannelOpenListener());
        context.bind();
    }

    class HttpProxyCodec extends HttpCodec {

        @Override
        public Frame decode(final Channel ch_src, ByteBuf src) throws Exception {
            ProxySession s = ProxySession.get(ch_src);
            if (s.handshakeFinished) {
                if (s.connector == null || !s.connector.isConnected()) {
                    NioEventLoop el = ch_src.getEventLoop();
                    ChannelConnector context = new ChannelConnector(el, s.host, s.port);
                    context.addProtocolCodec(new ProtocolCodec() {

                        @Override
                        public String getProtocolId() {
                            return "http-proxy-connect";
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
                        public Frame decode(Channel ch, ByteBuf src) throws IOException {
                            ByteBuf buf = ch_src.alloc().allocate(src.remaining());
                            buf.put(src);
                            ch_src.writeAndFlush(buf.flip());
                            return null;
                        }
                    });
                    context.setPrintConfig(false);
                    context.addChannelEventListener(new LoggerChannelOpenListener());
                    ByteBuf buf = ch_src.alloc().allocate(src.remaining());
                    buf.put(src);
                    s.connector = context;
                    s.connector.connect((ch_target, ex) -> {
                        if (ex == null) {
                            ch_target.writeAndFlush(buf.flip());
                        } else {
                            buf.release();
                            ProxySession.remove(ch_src);
                            Util.close(ch_src);
                        }
                    });
                } else {
                    ByteBuf buf = ch_src.alloc().allocate(src.remaining());
                    buf.put(src);
                    s.connector.getChannel().writeAndFlush(buf.flip());
                }
                return null;
            }
            return super.decode(ch_src, src);
        }

        @Override
        protected void parse_line_one(HttpFrame f, CharSequence line) {
            if (line.charAt(0) == 'C' && line.charAt(1) == 'O' && line.charAt(2) == 'N'
                    && line.charAt(3) == 'N' && line.charAt(4) == 'E' && line.charAt(5) == 'C'
                    && line.charAt(6) == 'T' && line.charAt(7) == ' ') {
                f.setMethod(HttpMethod.CONNECT);
                parseRequestURL(f, 8, line);
            } else {
                super.parse_line_one(f, line);
            }
        }

    }

    static class ProxySession {

        static final String     ProxySessionChAttr = "_ProxySessionChAttr";
        public boolean          handshakeFinished;
        public String           host;
        public int              port;
        public ChannelConnector connector;

        @Override
        public String toString() {
            return host + ":" + port;
        }

        public static void remove(Channel ch) {
            ProxySession s = (ProxySession) ch.removeAttribute(ProxySessionChAttr);
            if (s != null) {
                s.connector = null;
            }
        }

        public static ProxySession get(Channel ch) {
            ProxySession s = (ProxySession) ch.getAttribute(ProxySessionChAttr);
            if (s == null) {
                s = new ProxySession();
                ch.setAttribute(ProxySessionChAttr, s);
            }
            return s;
        }
    }

    public static HttpProxyServer get() {
        return server;
    }

    public static void main(String[] args) throws Exception {
        get().strtup(new NioEventLoopGroup(true), 8088);
    }

}
