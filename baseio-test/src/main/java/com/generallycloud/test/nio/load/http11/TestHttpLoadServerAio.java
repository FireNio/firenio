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
package com.generallycloud.test.nio.load.http11;

import java.util.concurrent.atomic.AtomicInteger;

import com.generallycloud.nio.acceptor.AioChannelAcceptor;
import com.generallycloud.nio.codec.http11.ServerHTTPProtocolFactory;
import com.generallycloud.nio.codec.http11.future.HttpReadFuture;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.AioSocketChannelContext;
import com.generallycloud.nio.component.IoEventHandleAdaptor;
import com.generallycloud.nio.component.LoggerSocketSEListener;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.protocol.ReadFuture;

public class TestHttpLoadServerAio {

	public static void main(String[] args) throws Exception {
		
		final AtomicInteger res = new AtomicInteger();
		final AtomicInteger req = new AtomicInteger();

		IoEventHandleAdaptor eventHandleAdaptor = new IoEventHandleAdaptor() {

			@Override
			public void accept(SocketSession session, ReadFuture future) throws Exception {
				HttpReadFuture f = (HttpReadFuture) future;

				String res;

				if (f.hasBodyContent()) {

					byte[] array = f.getBodyContent();

					res = "yes server already accept your message :) </BR><PRE style='font-size: 18px;color: #FF9800;'>"
							+ new String(array) + "</PRE>";
				} else {
					res = "yes server already accept your message :) " + f.getRequestParams();
				}

				f.write(res);
				session.flush(f);
//				System.out.println("req======================"+req.getAndIncrement());
			}
			
			@Override
			public void futureSent(SocketSession session, ReadFuture future) {
//				System.out.println("res==========="+res.getAndIncrement());
			}
		};
		
		ServerConfiguration c = new ServerConfiguration(80);
		
		c.setSERVER_MEMORY_POOL_CAPACITY(2560000);
		c.setSERVER_MEMORY_POOL_UNIT(256);
		c.setSERVER_ENABLE_MEMORY_POOL_DIRECT(true);
		c.setSERVER_CORE_SIZE(2);
		c.setSERVER_ENABLE_MEMORY_POOL(true);
		c.setSERVER_MEMORY_POOL_CAPACITY_RATE(0.5);

		AioSocketChannelContext context = new AioSocketChannelContext(c);

		context.setProtocolFactory(new ServerHTTPProtocolFactory());
		context.setIoEventHandleAdaptor(eventHandleAdaptor);
		context.addSessionEventListener(new LoggerSocketSEListener());
		
		new AioChannelAcceptor(context).bind();
		
		ThreadUtil.sleep(99999999);
	}
}
