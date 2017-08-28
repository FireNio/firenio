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

import com.generallycloud.baseio.codec.http11.ClientHTTPProtocolFactory;
import com.generallycloud.baseio.codec.http11.HttpClient;
import com.generallycloud.baseio.codec.http11.HttpIOEventHandle;
import com.generallycloud.baseio.codec.http11.future.HttpFuture;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.component.LoggerSocketSEListener;
import com.generallycloud.baseio.component.NioSocketChannelContext;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.configuration.ServerConfiguration;
import com.generallycloud.baseio.connector.SocketChannelConnector;
import com.generallycloud.test.io.common.FutureFactory;

public class TestSimpleHttpClient {

    public static void main(String[] args) throws Exception {

        HttpIOEventHandle eventHandleAdaptor = new HttpIOEventHandle();

        //		ServerConfiguration c = new ServerConfiguration("localhost",80);

        ServerConfiguration c = new ServerConfiguration("generallycloud.com", 443);

        SocketChannelContext context = new NioSocketChannelContext(c);

        SocketChannelConnector connector = new SocketChannelConnector(context);

        context.setProtocolFactory(new ClientHTTPProtocolFactory());
        context.setIoEventHandleAdaptor(eventHandleAdaptor);
        context.addSessionEventListener(new LoggerSocketSEListener());

        SocketSession session = connector.connect();

        HttpClient client = new HttpClient(session);

        HttpFuture future = FutureFactory.createHttpReadFuture(session, "/test");

        HttpFuture res = client.request(future);
        System.out.println();
        System.out.println(new String(res.getBodyContent()));
        System.out.println();
        CloseUtil.close(connector);

    }
}
