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
package test.io.http11;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.firenio.codec.http11.ClientHttpCodec;
import com.firenio.codec.http11.ClientHttpFrame;
import com.firenio.codec.http11.HttpFrame;
import com.firenio.codec.http11.HttpHeader;
import com.firenio.codec.http11.WebSocketCodec;
import com.firenio.codec.http11.WebSocketFrame;
import com.firenio.codec.http11.WsUpgradeRequestFrame;
import com.firenio.common.Util;
import com.firenio.component.ChannelConnector;
import com.firenio.component.Frame;
import com.firenio.component.IoEventHandle;
import com.firenio.component.LoggerChannelOpenListener;
import com.firenio.component.NioEventLoopGroup;
import com.firenio.component.Channel;
import com.firenio.component.SslContextBuilder;

public class TestSimpleWebSocketClient {

    public static void main(String[] args) throws Exception {

        IoEventHandle eventHandleAdaptor = new IoEventHandle() {

            @Override
            public void accept(Channel ch, Frame frame) throws Exception {
                if (frame instanceof ClientHttpFrame) {
                    ClientHttpFrame f = (ClientHttpFrame) frame;
                    if (f.updateWebSocketProtocol(ch)) {
                        WebSocketFrame f2 = new WebSocketFrame();
                        Map<String, String> map = new HashMap<>();
                        map.put("action", "add-user");
                        map.put("username", "火星人" + Util.randomUUID());
                        f2.setContent(ch.allocate());
                        f2.write(JSON.toJSONString(map), ch);
                        ch.writeAndFlush(f2);
                    }
                    System.out.println(f.getResponse_headers());
                } else {
                    WebSocketFrame f = (WebSocketFrame) frame;
                    System.out.println(f.getStringContent());
                }
            }
        };

        String host = "www.firenio.com";
        int port = 443;
        NioEventLoopGroup g = new NioEventLoopGroup();
        g.setEnableMemoryPool(false);
        ChannelConnector context = new ChannelConnector(g, host, 443);
        //        context.setExecutorEventLoopGroup(new ExecutorEventLoopGroup());
        context.setIoEventHandle(eventHandleAdaptor);
        context.addProtocolCodec(new ClientHttpCodec());
        context.addProtocolCodec(new WebSocketCodec());
        context.addChannelEventListener(new LoggerChannelOpenListener());
        context.setSslContext(SslContextBuilder.forClient(true).build());
        Channel ch = context.connect();
        String url = "/web-socket-chat";
        HttpFrame frame = new WsUpgradeRequestFrame(url);
        frame.setRequestHeader(HttpHeader.Host, host + port);
        frame.setRequestHeader(HttpHeader.Pragma, "no-cache");
        frame.setRequestHeader(HttpHeader.Cache_Control, "no-cache");
        frame.setRequestHeader(HttpHeader.User_Agent,
                "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.143 Safari/537.36");
        frame.setRequestHeader(HttpHeader.Accept_Encoding, "gzip, deflate, sdch");
        frame.setRequestHeader(HttpHeader.Accept_Language, "zh-CN,zh;q=0.8");
        ch.writeAndFlush(frame);
        Util.sleep(999999999);
        Util.close(context);

    }
}
