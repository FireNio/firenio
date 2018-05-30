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
import com.generallycloud.baseio.codec.http11.HttpClient;
import com.generallycloud.baseio.codec.http11.HttpFuture;
import com.generallycloud.baseio.codec.http11.HttpIOEventHandle;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.component.ChannelConnector;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.LoggerSocketSEListener;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.component.ssl.SSLUtil;
import com.generallycloud.baseio.component.ssl.SslContext;
import com.generallycloud.baseio.configuration.Configuration;

public class TestSimpleHttpClient {

    public static void main(String[] args) throws Exception {

        HttpIOEventHandle eventHandleAdaptor = new HttpIOEventHandle();

        //		ServerConfiguration c = new ServerConfiguration("localhost",80);

        Configuration c = new Configuration("generallycloud.com", 443);

        ChannelContext context = new ChannelContext(c);

        ChannelConnector connector = new ChannelConnector(context);

        SslContext sslContext = SSLUtil.initClient(true);

        context.setProtocolCodec(new ClientHttpCodec());
        context.setIoEventHandleAdaptor(eventHandleAdaptor);
        context.addSessionEventListener(new LoggerSocketSEListener());
        context.setSslContext(sslContext);

        SocketSession session = connector.connect();

        HttpClient client = new HttpClient(session);

        HttpFuture future = new ClientHttpFuture(context, "/test-show-memory");

        HttpFuture res = client.request(future, 10000);
        System.out.println();
        System.out.println(new String(res.getBodyContent()));
        System.out.println();

        CloseUtil.close(connector);

    }
}
