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

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.UnpooledByteBufAllocator;
import com.generallycloud.baseio.codec.http11.ClientHttpCodec;
import com.generallycloud.baseio.codec.http11.ClientHttpFrame;
import com.generallycloud.baseio.codec.http11.HttpFrame;
import com.generallycloud.baseio.codec.http11.HttpMethod;
import com.generallycloud.baseio.codec.http11.HttpStatus;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.component.ChannelAcceptor;
import com.generallycloud.baseio.component.ChannelConnector;
import com.generallycloud.baseio.component.IoEventHandle;
import com.generallycloud.baseio.component.LoggerChannelOpenListener;
import com.generallycloud.baseio.component.NioEventLoopGroup;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.protocol.Frame;
import com.generallycloud.sample.baseio.http11.proxy.HttpProxyCodec.ProxySession;

public class HttpProxyServer {

    static final byte[]                 CONNECT_RES     = "HTTP/1.1 200 Connection Established\r\n\r\n".getBytes();
    static final ByteBuf                CONNECT_RES_BUF = UnpooledByteBufAllocator.getHeap().wrap(CONNECT_RES);
    static final HttpProxyServer        server          = new HttpProxyServer();
    private ChannelAcceptor              context;
    
    private volatile boolean enable = true;
    
    public synchronized void stop() {
        CloseUtil.unbind(context);
    }
    
    public void enable(){
        enable = true;
    }
    
    public void disable(){
        enable = false;
    }

    public synchronized void strtup(NioEventLoopGroup serverG, int port) throws Exception {
        if (context != null && context.isActive()) {
            return;
        }

        IoEventHandle eventHandle = new IoEventHandle() {

            @Override
            public void accept(NioSocketChannel channel, Frame frame) throws Exception {
                final HttpFrame f = (HttpFrame) frame;
                if (!enable) {
                    f.setStatus(HttpStatus.C503);
                    f.write("503 service unavailable".getBytes());
                    channel.flush(f);
                    return;
                }
                if (f.getMethod() == HttpMethod.CONNECT) {
                    channel.flush(CONNECT_RES_BUF.duplicate());
                    ProxySession s = ProxySession.get(channel);
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
                    ChannelConnector context = new ChannelConnector(channel.getEventLoop(),arr[0], port);
                    context.setProtocolCodec(new ClientHttpCodec());
                    context.setIoEventHandle(new IoEventHandle() {

                        @Override
                        public void accept(NioSocketChannel ch, Frame frame) throws Exception {
                            ClientHttpFrame res = (ClientHttpFrame) frame;
                            if (res.getBodyContent() != null) {
                                f.write(res.getBodyContent());
                            }
                            channel.flush(f);
                            ch.close();
                        }
                    });
                    final String url;
                    String uri = f.getRequestURI();
                    int index = uri.indexOf("/", 8);
                    if (index != -1) {
                        url = uri.substring(index);
                    } else {
                        url = "/";
                    }
                    context.addChannelEventListener(new LoggerChannelOpenListener());
                    context.connect((ch, ex) -> {
                        if (ex == null) {
                            HttpFrame req = new ClientHttpFrame(url, f.getMethod());
                            req.setRequestParams(f.getRequestParams());
                            ch.flush(req);
                        }
                    });
                }
            }
        };

        context = new ChannelAcceptor(serverG, 8088);
        context.setProtocolCodec(new HttpProxyCodec());
        context.setIoEventHandle(eventHandle);
        context.addChannelEventListener(new LoggerChannelOpenListener());
        context.bind();
    }

    public static HttpProxyServer get() {
        return server;
    }
    
    public static void main(String[] args) throws Exception {
        get().strtup(new NioEventLoopGroup(true), 8088);
    }
    

}
