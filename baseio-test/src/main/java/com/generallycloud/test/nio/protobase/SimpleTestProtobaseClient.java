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
package com.generallycloud.test.nio.protobase;

import com.generallycloud.nio.codec.protobase.ProtobaseProtocolFactory;
import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFuture;
import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFutureImpl;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.IoEventHandleAdaptor;
import com.generallycloud.nio.component.LoggerSocketSEListener;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.SocketChannelContextImpl;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.protocol.ReadFuture;

public class SimpleTestProtobaseClient {

	public static void main(String[] args) throws Exception {

		IoEventHandleAdaptor eventHandleAdaptor = new IoEventHandleAdaptor() {

			@Override
			public void accept(SocketSession session, ReadFuture future) throws Exception {
				System.out.println();
				System.out.println("____________________"+future.getReadText());
				System.out.println();
			}
		};
		
		SocketChannelContext context = new SocketChannelContextImpl(new ServerConfiguration("localhost", 18300));

		context.getServerConfiguration().setSERVER_ENABLE_MEMORY_POOL_DIRECT(true);
		
		SocketChannelConnector connector = new SocketChannelConnector(context);
		
		connector.setTimeout(99999999);

		context.setIoEventHandleAdaptor(eventHandleAdaptor);
		
		context.addSessionEventListener(new LoggerSocketSEListener());

		context.setProtocolFactory(new ProtobaseProtocolFactory());
		
		SocketSession session = connector.connect();

		ProtobaseReadFuture future = new ProtobaseReadFutureImpl(context,"test222");

		future.write("hello server!");

		session.flush(future);
		
		ThreadUtil.sleep(100);

		CloseUtil.close(connector);
	}
}
