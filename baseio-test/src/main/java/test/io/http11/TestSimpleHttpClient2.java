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
package test.io.http11;

import com.firenio.baseio.codec.http11.ClientHttpCodec;
import com.firenio.baseio.codec.http11.ClientHttpFrame;
import com.firenio.baseio.codec.http11.HttpMethod;
import com.firenio.baseio.codec.http11.WebSocketCodec;
import com.firenio.baseio.component.Channel;
import com.firenio.baseio.component.ChannelConnector;
import com.firenio.baseio.component.Frame;
import com.firenio.baseio.component.IoEventHandle;
import com.firenio.baseio.component.LoggerChannelOpenListener;
import com.firenio.baseio.component.SslContextBuilder;

/**
 * @author wangkai
 *
 */
public class TestSimpleHttpClient2 {

    //telnet 192.168.133.134 8080
    public static void main(String[] args) throws Exception {

        String host = "www.baidu.com";
        String url = "/plaintext";
        host = "firenio.com";
        host = "127.0.0.1";
        //        host = "fe80::a793:9577:4396:8ca6";
        //        host = "www.baidu.com";
//        host = "api.weixin.qq.com";
//        host = "192.168.1.103";
        int port = 1443;
        port = 8080;
        //        port = 443;

        ChannelConnector context = new ChannelConnector(host, port);
        context.addProtocolCodec(new ClientHttpCodec());
        context.addProtocolCodec(new WebSocketCodec());
        context.setIoEventHandle(new IoEventHandle() {

            @Override
            public void accept(Channel ch, Frame frame) throws Exception {
                ClientHttpFrame res = (ClientHttpFrame) frame;
                System.out.println();
                System.out.println(new String(res.getArrayContent()));
                System.out.println();
                //                Util.close(context);
            }
        });
        context.addChannelEventListener(new LoggerChannelOpenListener());
        if (port == 1443 || port == 443) {
            context.setSslContext(SslContextBuilder.forClient(true).build());
        }
        Channel ch = context.connect(999999);
        ClientHttpFrame f = new ClientHttpFrame(url, HttpMethod.GET);
        //        f.setRequestHeader(HttpHeader.Host, host);
        //        f.setContent("abc123".getBytes());
        ch.write(f);
        ch.writeAndFlush(f);

    }
}
