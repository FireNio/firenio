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
package com.generallycloud.test.io.http11;

import com.generallycloud.baseio.codec.http11.ClientHttpCodec;
import com.generallycloud.baseio.codec.http11.ClientHttpFrame;
import com.generallycloud.baseio.codec.http11.HttpFrame;
import com.generallycloud.baseio.codec.http11.WebSocketFrame;
import com.generallycloud.baseio.codec.http11.WsUpgradeRequestFrame;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.ThreadUtil;
import com.generallycloud.baseio.component.ChannelConnector;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.IoEventHandle;
import com.generallycloud.baseio.component.LoggerChannelOpenListener;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.component.SslContextBuilder;
import com.generallycloud.baseio.protocol.Frame;

public class TestSimpleWebSocketClient {

    public static void main(String[] args) throws Exception {

        IoEventHandle eventHandleAdaptor = new IoEventHandle() {

            @Override
            public void accept(NioSocketChannel channel, Frame frame) throws Exception {
                if (frame instanceof ClientHttpFrame) {
                    ClientHttpFrame f = (ClientHttpFrame) frame;
                    if (f.updateWebSocketProtocol(channel)) {
                        WebSocketFrame f2 = new WebSocketFrame();
                        f2.write("{action: \"add-user\", username: \"火星人\"}", channel);
                        channel.flush(f2);
                    }
                    System.out.println(f.getRequestHeaders());
                } else {
                    WebSocketFrame f = (WebSocketFrame) frame;
                    System.out.println(f.getReadText());
                }
            }
        };

        ChannelContext context = new ChannelContext(443);
        ChannelConnector connector = new ChannelConnector(context);
        context.setIoEventHandle(eventHandleAdaptor);
        context.setProtocolCodec(new ClientHttpCodec());
        context.addChannelEventListener(new LoggerChannelOpenListener());
        context.setSslContext(SslContextBuilder.forClient(true).build());
        NioSocketChannel channel = connector.connect();
        String url = "/web-socket-chat";
        //        url = "/c1020";
        HttpFrame frame = new WsUpgradeRequestFrame(url);
        //		 frame.setRequestURL("ws://120.76.222.210:30005/");
        //		frame.setResponseHeader("Host", "120.76.222.210:30005");
        //		frame.setResponseHeader("Pragma", "no-cache");
        //		frame.setResponseHeader("Cache-Control", "no-cache");
        //		frame.setResponseHeader("User-Agent",
        //				"Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.143 Safari/537.36");
        //		frame.setResponseHeader("Accept-Encoding", "gzip, deflate, sdch");
        //		frame.setResponseHeader("Accept-Language", "zh-CN,zh;q=0.8");
        // frame.setRequestHeader("", "");
        channel.flush(frame);
        ThreadUtil.sleep(999999999);
        CloseUtil.close(connector);

    }
}
