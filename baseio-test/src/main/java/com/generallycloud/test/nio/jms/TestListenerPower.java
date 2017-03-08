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

import com.generallycloud.baseio.connector.SocketChannelConnector;
import com.generallycloud.baseio.container.FixedSession;
import com.generallycloud.baseio.container.SimpleIOEventHandle;
import com.generallycloud.baseio.container.jms.Message;
import com.generallycloud.baseio.container.jms.client.MessageConsumer;
import com.generallycloud.baseio.container.jms.client.OnMessage;
import com.generallycloud.baseio.container.jms.client.impl.DefaultMessageConsumer;
import com.generallycloud.test.nio.common.IoConnectorUtil;

public class TestListenerPower {

	public static void main(String[] args) throws Exception {

		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		SocketChannelConnector connector = IoConnectorUtil.getTCPConnector(eventHandle);

		FixedSession session = new FixedSession(connector.connect());

		session.login("admin", "admin100");
		
		MessageConsumer consumer = new DefaultMessageConsumer(session);
		long old = System.currentTimeMillis();
		
		OnMessage onMessage = new OnMessage() {
			
			@Override
			public void onReceive(Message message) {
				System.out.println(message);
			}
		};
		
		
		for (int i = 0; i < 10000; i++) {
			consumer.receive(onMessage);
		}

		System.out.println("Time:" + (System.currentTimeMillis() - old));

		connector.close();
	}
}
