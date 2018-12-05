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

import java.io.IOException;

import com.firenio.baseio.codec.http11.ClientHttpCodec;
import com.firenio.baseio.codec.http11.ClientHttpFrame;
import com.firenio.baseio.common.Util;
import com.firenio.baseio.component.ChannelConnector;
import com.firenio.baseio.component.IoEventHandle;
import com.firenio.baseio.component.LoggerChannelOpenListener;
import com.firenio.baseio.component.NioSocketChannel;
import com.firenio.baseio.component.SslContextBuilder;
import com.firenio.baseio.protocol.Frame;

/**
 * @author wangkai
 *
 */
public class TestSimpleHttpClient2 {

    public static void main(String[] args) throws IOException {

        String host = "www.baidu.com";
        host = "firenio.com";
        host = "127.0.0.1";
        host = "www.baidu.com";
        int port = 443;
//        port = 80;

        ChannelConnector context = new ChannelConnector(host, port);
        context.setProtocolCodec(new ClientHttpCodec());
        context.setIoEventHandle(new IoEventHandle() {

            @Override
            public void accept(NioSocketChannel ch, Frame frame) throws Exception {
                ClientHttpFrame res = (ClientHttpFrame) frame;
                System.out.println();
                System.out.println(new String(res.getBodyContent()));
                System.out.println();
                Util.close(context);
            }
        });
        context.addChannelEventListener(new LoggerChannelOpenListener());
        if (port == 443) {
            context.setSslContext(SslContextBuilder.forClient(true).build());
        }
        NioSocketChannel channel = context.connect();
        channel.flush(new ClientHttpFrame("/"));

    }
}
