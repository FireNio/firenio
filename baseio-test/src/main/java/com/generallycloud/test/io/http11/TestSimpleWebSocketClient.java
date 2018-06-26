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
import com.generallycloud.baseio.codec.http11.ClientHttpFuture;
import com.generallycloud.baseio.codec.http11.HttpFuture;
import com.generallycloud.baseio.codec.http11.WebSocketFuture;
import com.generallycloud.baseio.codec.http11.WsUpgradeRequestFuture;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.ThreadUtil;
import com.generallycloud.baseio.component.ChannelConnector;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.IoEventHandle;
import com.generallycloud.baseio.component.LoggerChannelOpenListener;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.component.ssl.SSLUtil;
import com.generallycloud.baseio.protocol.Future;

public class TestSimpleWebSocketClient {

    public static void main(String[] args) throws Exception {

        IoEventHandle eventHandleAdaptor = new IoEventHandle() {

            @Override
            public void accept(NioSocketChannel channel, Future future) throws Exception {
                if (future instanceof ClientHttpFuture) {
                    ClientHttpFuture f = (ClientHttpFuture) future;
                    if (f.updateWebSocketProtocol(channel)) {
                        WebSocketFuture f2 = new WebSocketFuture();
                        f2.write("{action: \"add-user\", username: \"火星人\"}", channel);
                        channel.flush(f2);
                    }
                    System.out.println(f.getRequestHeaders());
                } else {
                    WebSocketFuture f = (WebSocketFuture) future;
                    System.out.println(f.getReadText());
                }
            }
        };

        ChannelContext context = new ChannelContext(443);
        ChannelConnector connector = new ChannelConnector(context);
        context.setIoEventHandle(eventHandleAdaptor);
        context.setProtocolCodec(new ClientHttpCodec());
        context.addChannelEventListener(new LoggerChannelOpenListener());
        context.setSslContext(SSLUtil.initClient(true));
        NioSocketChannel channel = connector.connect();
        String url = "/web-socket-chat";
        //        url = "/c1020";
        HttpFuture future = new WsUpgradeRequestFuture(channel.getContext(), url);
        //		 future.setRequestURL("ws://120.76.222.210:30005/");
        //		future.setResponseHeader("Host", "120.76.222.210:30005");
        //		future.setResponseHeader("Pragma", "no-cache");
        //		future.setResponseHeader("Cache-Control", "no-cache");
        //		future.setResponseHeader("User-Agent",
        //				"Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.143 Safari/537.36");
        //		future.setResponseHeader("Accept-Encoding", "gzip, deflate, sdch");
        //		future.setResponseHeader("Accept-Language", "zh-CN,zh;q=0.8");
        // future.setRequestHeader("", "");
        channel.flush(future);
        ThreadUtil.sleep(999999999);
        CloseUtil.close(connector);

    }
}
