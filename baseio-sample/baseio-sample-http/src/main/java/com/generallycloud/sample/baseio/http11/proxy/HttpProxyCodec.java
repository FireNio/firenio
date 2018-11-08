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
package com.generallycloud.sample.baseio.http11.proxy;

import java.io.IOException;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.codec.http11.HttpCodec;
import com.generallycloud.baseio.codec.http11.HttpFrame;
import com.generallycloud.baseio.codec.http11.HttpMethod;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.component.ChannelConnector;
import com.generallycloud.baseio.component.LoggerChannelOpenListener;
import com.generallycloud.baseio.component.NioEventLoopGroup;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.protocol.Frame;
import com.generallycloud.baseio.protocol.ProtocolCodec;

/**
 * @author wangkai
 *
 */
public class HttpProxyCodec extends HttpCodec {

    NioEventLoopGroup g;

    public HttpProxyCodec(NioEventLoopGroup g) {
        this.g = g;
    }

    @Override
    public Frame decode(final NioSocketChannel ch_p, ByteBuf src) throws IOException {
        ProxySession s = ProxySession.get(ch_p);
        if (s.handshakeFinished) {
            if (s.connector == null || !s.connector.isConnected()) {
                ChannelConnector context = new ChannelConnector(s.host, s.port);
                context.setProtocolCodec(new ProtocolCodec() {

                    @Override
                    public String getProtocolId() {
                        return "http-proxy-connect";
                    }

                    @Override
                    public ByteBuf encode(NioSocketChannel ch, Frame frame) throws IOException {
                        return null;
                    }

                    @Override
                    public Frame decode(NioSocketChannel ch, ByteBuf src) throws IOException {
                        ByteBuf buf = ch_p.alloc().allocate(src.remaining());
                        buf.read(src);
                        ch_p.flush(buf.flip());
                        return null;
                    }
                });
                context.setNioEventLoopGroup(g);
                context.addChannelEventListener(new LoggerChannelOpenListener());
                ByteBuf buf = ch_p.alloc().allocate(src.remaining());
                buf.read(src);
                s.connector = context;
                    s.connector.connect((ch, ex) -> {
                        if (ex == null) {
                            s.connector.getChannel().flush(buf.flip());
                        }else{
                            buf.release();
                            ProxySession.remove(ch_p);
                            CloseUtil.close(ch_p);
                        }
                    });
            }else{
                ByteBuf buf = ch_p.alloc().allocate(src.remaining());
                buf.read(src);
                s.connector.getChannel().flush(buf.flip());
            }
            return null;
        }
        return super.decode(ch_p, src);
    }

    @Override
    protected void parseFirstLine(HttpFrame f, StringBuilder line) {
        if (line.charAt(0) == 'C' 
                && line.charAt(1) == 'O' 
                && line.charAt(2) == 'N'
                && line.charAt(3) == 'N' 
                && line.charAt(4) == 'E' 
                && line.charAt(5) == 'C'
                && line.charAt(6) == 'T' 
                && line.charAt(7) == ' ') {
            f.setMethod(HttpMethod.CONNECT);
            parseRequestURL(f, 8, line);
        } else {
            super.parseFirstLine(f, line);
        }
    }
    
    public static class ProxySession {

        static final String     ProxySessionChAttr = "_ProxySessionChAttr";
        public boolean          handshakeFinished;
        public String           host;
        public int              port;
        public ChannelConnector connector;

        @Override
        public String toString() {
            return host + ":" + port;
        }

        public static void remove(NioSocketChannel ch) {
            ProxySession s = (ProxySession) ch.removeAttribute(ProxySessionChAttr);
            if (s != null) {
                s.connector = null;
            }
        }

        public static ProxySession get(NioSocketChannel ch) {
            ProxySession s = (ProxySession) ch.getAttribute(ProxySessionChAttr);
            if (s == null) {
                s = new ProxySession();
                ch.setAttribute(ProxySessionChAttr, s);
            }
            return s;
        }
    }

}
