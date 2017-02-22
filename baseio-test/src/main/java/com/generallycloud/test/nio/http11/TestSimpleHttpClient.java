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
package com.generallycloud.test.nio.http11;

import com.generallycloud.nio.codec.http11.ClientHTTPProtocolFactory;
import com.generallycloud.nio.codec.http11.HttpClient;
import com.generallycloud.nio.codec.http11.HttpIOEventHandle;
import com.generallycloud.nio.codec.http11.future.HttpReadFuture;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.component.LoggerSocketSEListener;
import com.generallycloud.nio.component.NioSocketChannelContext;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.test.nio.common.ReadFutureFactory;

public class TestSimpleHttpClient {

	public static void main(String[] args) throws Exception {
		
		HttpIOEventHandle eventHandleAdaptor = new HttpIOEventHandle();

		ServerConfiguration c = new ServerConfiguration("localhost",80);
		
		SocketChannelContext context = new NioSocketChannelContext(c);
		
		SocketChannelConnector connector = new SocketChannelConnector(context);

		context.setProtocolFactory(new ClientHTTPProtocolFactory());
		context.setIoEventHandleAdaptor(eventHandleAdaptor);
		context.addSessionEventListener(new LoggerSocketSEListener());

		SocketSession session = connector.connect();

		HttpClient client = new HttpClient(session);

		HttpReadFuture future = ReadFutureFactory.createHttpReadFuture(session, "/test");

		HttpReadFuture res = client.request(future);
		System.out.println();
		System.out.println(new String(res.getBodyContent()));
		System.out.println();
		CloseUtil.close(connector);

	}
}
