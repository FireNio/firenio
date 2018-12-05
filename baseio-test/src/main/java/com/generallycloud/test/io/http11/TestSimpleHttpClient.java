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
package com.generallycloud.test.io.http11;

import com.generallycloud.baseio.codec.http11.ClientHttpCodec;
import com.generallycloud.baseio.codec.http11.ClientHttpFrame;
import com.generallycloud.baseio.common.Util;
import com.generallycloud.baseio.component.ChannelConnector;
import com.generallycloud.baseio.component.IoEventHandle;
import com.generallycloud.baseio.component.LoggerChannelOpenListener;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.component.SslContext;
import com.generallycloud.baseio.component.SslContextBuilder;
import com.generallycloud.baseio.protocol.Frame;

public class TestSimpleHttpClient {

    public static void main(String[] args) throws Exception {

        ChannelConnector context = new ChannelConnector("generallycloud.com", 443);
        SslContext sslContext = SslContextBuilder.forClient(true).build();
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
        context.setSslContext(sslContext);
        NioSocketChannel channel = context.connect();
        channel.flush(new ClientHttpFrame("/test-show-memory"));
    }
    
}
