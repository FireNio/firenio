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
import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFuture;
import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFutureImpl;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.IoEventHandleAdaptor;
import com.generallycloud.nio.component.LoggerSocketSEListener;
import com.generallycloud.nio.component.NioSocketChannelContext;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.protocol.ReadFuture;

public class TestSimple {
	
	
	public static void main(String[] args) throws Exception {

		SharedBundle.instance().loadAllProperties("nio");

		String serviceKey = "/test-simple";
		
		String param = "ttt";
		
		IoEventHandleAdaptor eventHandle = new IoEventHandleAdaptor() {
			
			@Override
			public void accept(SocketSession session, ReadFuture future) throws Exception {
				System.out.println("________________________"+future.getReadText());
			}
		};
		
		SocketChannelContext context = new NioSocketChannelContext(new ServerConfiguration(18300));
		
		SocketChannelConnector connector = new SocketChannelConnector(context);

		context.setProtocolFactory(new ProtobaseProtocolFactory());
		
		context.addSessionEventListener(new LoggerSocketSEListener());
		
		context.setIoEventHandleAdaptor(eventHandle);
		
		SocketSession session = connector.connect();

		ProtobaseReadFuture f = new ProtobaseReadFutureImpl(connector.getContext(),serviceKey);
		
		f.write(param);
		
		session.flush(f);
		
		ThreadUtil.sleep(500);
		
		CloseUtil.close(connector);
	}
}
