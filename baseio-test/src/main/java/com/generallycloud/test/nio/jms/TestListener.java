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
package com.generallycloud.test.nio.jms;

import com.generallycloud.nio.codec.protobase.ProtobaseProtocolFactory;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.container.FixedSession;
import com.generallycloud.nio.container.SimpleIOEventHandle;
import com.generallycloud.nio.container.jms.Message;
import com.generallycloud.nio.container.jms.client.MessageConsumer;
import com.generallycloud.nio.container.jms.client.OnMessage;
import com.generallycloud.nio.container.jms.client.impl.DefaultMessageConsumer;
import com.generallycloud.test.nio.common.IoConnectorUtil;

public class TestListener {

	public static void main(String[] args) throws Exception {
		
		SharedBundle.instance().loadAllProperties("nio");

		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		SocketChannelConnector connector = IoConnectorUtil.getTCPConnector(eventHandle);
		
		connector.getContext().setProtocolFactory(new ProtobaseProtocolFactory());

		FixedSession session = new FixedSession(connector.connect());

		session.login("admin", "admin100");

		MessageConsumer consumer = new DefaultMessageConsumer(session);

		long old = System.currentTimeMillis();

		consumer.receive(new OnMessage() {

			@Override
			public void onReceive(Message message) {
				System.out.println(message);
			}
		});

		System.out.println("Time:" + (System.currentTimeMillis() - old));

		ThreadUtil.sleep(1500000);
		
		CloseUtil.close(connector);

	}

}
