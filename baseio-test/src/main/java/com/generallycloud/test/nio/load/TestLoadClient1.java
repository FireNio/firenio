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
package com.generallycloud.test.nio.load;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import com.generallycloud.nio.codec.fixedlength.FixedLengthProtocolFactory;
import com.generallycloud.nio.codec.fixedlength.future.FixedLengthReadFuture;
import com.generallycloud.nio.codec.fixedlength.future.FixedLengthReadFutureImpl;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.IoEventHandleAdaptor;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.protocol.ReadFuture;
import com.generallycloud.test.nio.common.IoConnectorUtil;
import com.generallycloud.test.test.ITestThread;
import com.generallycloud.test.test.ITestThreadHandle;

public class TestLoadClient1 extends ITestThread {

	private SocketChannelConnector	connector			= null;

	@Override
	public void run() {

		int time1 = getTime();

		SocketSession session = connector.getSession();

		for (int i = 0; i < time1; i++) {
			
			FixedLengthReadFuture future = new FixedLengthReadFutureImpl(session.getContext());
			
			future.write("hello server!");
			
			session.flush(future);
		}
	}

	@Override
	public void prepare() throws Exception {

		SharedBundle.instance().loadAllProperties("nio");

		IoEventHandleAdaptor eventHandleAdaptor = new IoEventHandleAdaptor() {
			@Override
			public void accept(SocketSession session, ReadFuture future) throws Exception {
				CountDownLatch latch = getLatch();

				latch.countDown();

//				 System.out.println("__________________________"+getLatch().getCount());
			}
		};

		connector = IoConnectorUtil.getTCPConnector(eventHandleAdaptor);

		SocketChannelContext context = connector.getContext();

		ServerConfiguration c = context.getServerConfiguration();

		c.setSERVER_MEMORY_POOL_CAPACITY(1280000);
		c.setSERVER_MEMORY_POOL_UNIT(256);
		
//		c.setSERVER_HOST("192.168.0.180");

		context.setProtocolFactory(new FixedLengthProtocolFactory());

		connector.connect();
	}

	@Override
	public void stop() {
		CloseUtil.close(connector);
	}

	public static void main(String[] args) throws IOException {

		SharedBundle.instance().loadAllProperties("nio");

		int time = 256 * 10000;

		int core_size = 4;

		ITestThreadHandle.doTest(TestLoadClient1.class, core_size, time / core_size);
	}
}
