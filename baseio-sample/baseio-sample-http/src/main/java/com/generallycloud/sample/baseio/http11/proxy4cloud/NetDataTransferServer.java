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
package com.generallycloud.sample.baseio.http11.proxy4cloud;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.component.ChannelAcceptor;
import com.generallycloud.baseio.component.ChannelConnector;
import com.generallycloud.baseio.component.LoggerChannelOpenListener;
import com.generallycloud.baseio.component.NioEventLoop;
import com.generallycloud.baseio.component.NioEventLoopGroup;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.protocol.Frame;
import com.generallycloud.baseio.protocol.ProtocolCodec;
import com.generallycloud.sample.baseio.http11.proxy4cloud.HttpProxy4CloudServer.ProxySession4Cloud;

/**
 * @author wangkai
 *
 */
public class NetDataTransferServer {

    public static final String host = "127.0.0.1";
    public static final int    port = 6666;
    
    public static void mask(ByteBuf src,byte m){
        ByteBuffer buf = src.nioBuffer();
        int p = buf.position();
        int l = buf.limit();
        for (; p < l; p++) {
            buf.put(p, (byte) (buf.get(p) ^ m));
        }
    }

    public static void main(String[] args) throws IOException {

        NioEventLoopGroup group = new NioEventLoopGroup();
        group.setSharable(true);
        ChannelAcceptor context = new ChannelAcceptor(group, port);
        context.setProtocolCodec(new ProtocolCodec() {

            @Override
            public String getProtocolId() {
                return "NetDataTransfer";
            }

            @Override
            public ByteBuf encode(NioSocketChannel ch, Frame frame) throws IOException {
                return null;
            }

            @Override
            public Frame decode(NioSocketChannel ch_src, ByteBuf src) throws IOException {
                ProxySession4Cloud s = ProxySession4Cloud.get(ch_src);
                if (s.handshakeFinished) {
                    if (s.connector == null || !s.connector.isConnected()) {
                        NioEventLoop el = ch_src.getEventLoop();
                        ChannelConnector context = new ChannelConnector(el, s.host, s.port);
                        context.setProtocolCodec(new ProtocolCodec() {

                            @Override
                            public String getProtocolId() {
                                return "http-proxy-connect";
                            }

                            @Override
                            public ByteBuf encode(NioSocketChannel ch, Frame frame)
                                    throws IOException {
                                return null;
                            }

                            @Override
                            public Frame decode(NioSocketChannel ch, ByteBuf src)
                                    throws IOException {
                                ByteBuf buf = ch_src.alloc().allocate(src.remaining());
                                buf.read(src);
                                buf.flip();
                                mask(buf, s.mask);
                                ch_src.flush(buf);
                                return null;
                            }
                        });
                        context.addChannelEventListener(new LoggerChannelOpenListener());
                        ByteBuf buf = ch_src.alloc().allocate(src.remaining());
                        buf.read(src);
                        buf.flip();
                        s.connector = context;
                        s.connector.connect((ch, ex) -> {
                            if (ex == null) {
                                mask(buf, s.mask);
                                ch.flush(buf);
                            } else {
                                buf.release();
                                ProxySession4Cloud.remove(ch_src);
                                CloseUtil.close(ch_src);
                            }
                        });
                    } else {
                        ByteBuf buf = ch_src.alloc().allocate(src.remaining());
                        buf.read(src);
                        buf.flip();
                        mask(buf, s.mask);
                        s.connector.getChannel().flush(buf);
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
        });
        context.addChannelEventListener(new LoggerChannelOpenListener());
        context.bind();
    }

}
