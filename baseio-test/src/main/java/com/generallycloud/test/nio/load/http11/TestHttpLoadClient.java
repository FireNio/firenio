/*
 * Copyright 2015 GenerallyCloud.com
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
package com.generallycloud.test.nio.load.http11;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import com.generallycloud.nio.codec.http11.ClientHTTPProtocolFactory;
import com.generallycloud.nio.codec.http11.future.HttpReadFuture;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.component.IoEventHandleAdaptor;
import com.generallycloud.nio.component.LoggerSocketSEListener;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.SocketChannelContextImpl;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.protocol.ReadFuture;
import com.generallycloud.test.nio.common.ReadFutureFactory;
import com.generallycloud.test.test.ITestThread;
import com.generallycloud.test.test.ITestThreadHandle;

public class TestHttpLoadClient extends ITestThread {

	private SocketChannelConnector	connector;

	private SocketSession			session;

	@Override
	public void run() {

		int time = getTime();

		for (int i = 0; i < time; i++) {

			HttpReadFuture future = ReadFutureFactory.createHttpReadFuture(session, "/test");

			session.flush(future);
		}
	}

	@Override
	public void prepare() throws Exception {

		IoEventHandleAdaptor eventHandleAdaptor = new IoEventHandleAdaptor() {
			@Override
			public void accept(SocketSession session, ReadFuture future) throws Exception {
				
				CountDownLatch latch = getLatch();

				latch.countDown();

//				System.out.println("__________________________"+getLatch().getCount());
			}
		};
		
		ServerConfiguration c = new ServerConfiguration("localhost",80);
		
		c.setSERVER_MEMORY_POOL_CAPACITY(1280000);
		c.setSERVER_MEMORY_POOL_UNIT(128);

		SocketChannelContext context = new SocketChannelContextImpl(c);
		
		connector = new SocketChannelConnector(context);

		context.setProtocolFactory(new ClientHTTPProtocolFactory());
		context.setIoEventHandleAdaptor(eventHandleAdaptor);
		context.addSessionEventListener(new LoggerSocketSEListener());

		session = connector.connect();
	}

	@Override
	public void stop() {
		CloseUtil.close(connector);
	}

	public static void main(String[] args) throws IOException {

		int time = 80 * 10000;

		int core_size = 4;

		ITestThreadHandle.doTest(TestHttpLoadClient.class, core_size, time / core_size);
	}

}
