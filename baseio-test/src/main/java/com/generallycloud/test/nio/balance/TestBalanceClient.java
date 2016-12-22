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
package com.generallycloud.test.nio.balance;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import com.generallycloud.nio.codec.protobase.ProtobaseProtocolFactory;
import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFuture;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.DateUtil;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.IoEventHandleAdaptor;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.protocol.ReadFuture;
import com.generallycloud.test.nio.common.IoConnectorUtil;
import com.generallycloud.test.nio.common.ReadFutureFactory;

public class TestBalanceClient {

	public static void main(String[] args) throws Exception {
		
		SharedBundle.instance().loadAllProperties("nio");
		
		final AtomicInteger res = new AtomicInteger();

		IoEventHandleAdaptor eventHandleAdaptor = new IoEventHandleAdaptor() {

			@Override
			public void accept(SocketSession session, ReadFuture future) throws Exception {
				
				ProtobaseReadFuture f = (ProtobaseReadFuture)future;
				
				System.out.println(f.getReadText()+"______R:"+System.currentTimeMillis());
				
				res.incrementAndGet();
			}
		};

		ServerConfiguration configuration = new ServerConfiguration();

		configuration.setSERVER_PORT(8600);

		SocketChannelConnector connector = IoConnectorUtil.getTCPConnector(eventHandleAdaptor, configuration);

		connector.getContext().setProtocolFactory(new ProtobaseProtocolFactory());
		
		connector.connect();

		SocketSession session = connector.getSession();
		
		for (int i = 0; i < 100; i++) {

			int fid = Math.abs(new Random().nextInt());
			
			ProtobaseReadFuture future = ReadFutureFactory.create(session,fid, "service-name");

			future.write("你好！");
			
			future.setHashCode(fid);

			session.flush(future);
		}
		
		ThreadUtil.sleep(500000000);

		CloseUtil.close(connector);
		
		System.out.println("=========="+res.get());
	}

}
