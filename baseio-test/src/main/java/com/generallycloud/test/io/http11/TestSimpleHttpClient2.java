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
import com.generallycloud.baseio.codec.http11.ClientHttpFuture;
import com.generallycloud.baseio.codec.http11.HttpClient;
import com.generallycloud.baseio.codec.http11.HttpFuture;
import com.generallycloud.baseio.codec.http11.HttpIOEventHandle;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.component.LoggerSocketSEListener;
import com.generallycloud.baseio.component.NioSocketChannelContext;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.component.ssl.SSLUtil;
import com.generallycloud.baseio.configuration.ServerConfiguration;
import com.generallycloud.baseio.connector.SocketChannelConnector;

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
        port = 8080;
        
        HttpIOEventHandle eventHandleAdaptor = new HttpIOEventHandle();
        ServerConfiguration c = new ServerConfiguration(host, port);
        SocketChannelContext context = new NioSocketChannelContext(c);
        SocketChannelConnector connector = new SocketChannelConnector(context);
        context.setProtocolCodec(new ClientHttpCodec());
        context.setIoEventHandleAdaptor(eventHandleAdaptor);
        context.addSessionEventListener(new LoggerSocketSEListener());
        if (port == 443) {
            context.setSslContext(SSLUtil.initClient(true));
        }
        SocketSession session = connector.connect();
        HttpClient client = new HttpClient(session);
        HttpFuture future = new ClientHttpFuture(context, "/");
        HttpFuture res = client.request(future,99990000);
        System.out.println();
        System.out.println(new String(res.getBodyContent()));
        System.out.println();
        
        CloseUtil.close(connector);
        
        
        
        
        
    }
}
