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

import java.io.IOException;

import com.generallycloud.baseio.codec.http11.ClientHttpCodec;
import com.generallycloud.baseio.codec.http11.ClientHttpFrame;
import com.generallycloud.baseio.codec.http11.HttpClient;
import com.generallycloud.baseio.codec.http11.HttpFrame;
import com.generallycloud.baseio.codec.http11.HttpIOEventHandle;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.component.ChannelConnector;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.LoggerChannelOpenListener;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.component.SslContextBuilder;

/**
 * @author wangkai
 *
 */
public class TestSimpleHttpClient2 {

    public static void main(String[] args) throws IOException {

        String host = "www.baidu.com";
        host = "generallycloud.com";
        host = "127.0.0.1";
        int port = 443;
        port = 8087;

        HttpIOEventHandle eventHandleAdaptor = new HttpIOEventHandle();
        ChannelContext context = new ChannelContext(host, port);
        ChannelConnector connector = new ChannelConnector(context);
        context.setProtocolCodec(new ClientHttpCodec());
        context.setIoEventHandle(eventHandleAdaptor);
        context.addChannelEventListener(new LoggerChannelOpenListener());
        if (port == 443) {
            context.setSslContext(SslContextBuilder.forClient(true).build());
        }
        NioSocketChannel channel = connector.connect();
        HttpClient client = new HttpClient(channel);
        HttpFrame frame = new ClientHttpFrame("/");
        HttpFrame res = client.request(frame, 99990000);
        System.out.println();
        System.out.println(new String(res.getBodyContent()));
        System.out.println();

        CloseUtil.close(connector);

    }
}
