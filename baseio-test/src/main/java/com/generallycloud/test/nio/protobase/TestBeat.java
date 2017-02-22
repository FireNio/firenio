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
package com.generallycloud.test.nio.protobase;

import com.generallycloud.nio.codec.protobase.ProtobaseProtocolFactory;
import com.generallycloud.nio.codec.protobase.future.ProtobaseBeatFutureFactory;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.NioSocketChannelContext;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.SocketSessionActiveSEListener;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.container.FixedSession;
import com.generallycloud.nio.container.SimpleIOEventHandle;
import com.generallycloud.nio.protocol.ReadFuture;

public class TestBeat {
	
	
	public static void main(String[] args) throws Exception {

		String serviceKey = "TestSimpleServlet";
		
		ServerConfiguration configuration = new ServerConfiguration(18300);

		configuration.setSERVER_SESSION_IDLE_TIME(100);
		
		SocketChannelContext context = new NioSocketChannelContext(configuration);
		
		SocketChannelConnector connector = new SocketChannelConnector(context);
		
		context.addSessionIdleEventListener(new SocketSessionActiveSEListener());
		
		context.setBeatFutureFactory(new ProtobaseBeatFutureFactory());
		
		context.setIoEventHandleAdaptor(new SimpleIOEventHandle());
		
		context.setProtocolFactory(new ProtobaseProtocolFactory());
		
		FixedSession session = new FixedSession(connector.connect());
		
		String param = "tttt";
		
		long old = System.currentTimeMillis();
		
		for (int i = 0; i < 5; i++) {
		
			ReadFuture future = session.request(serviceKey, param);
			System.out.println(future);
			ThreadUtil.sleep(1000);
			
		}
		System.out.println("Time:"+(System.currentTimeMillis() - old));
		
		CloseUtil.close(connector);
		
	}
}
