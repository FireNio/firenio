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

import com.generallycloud.baseio.common.SharedBundle;
import com.generallycloud.baseio.connector.SocketChannelConnector;
import com.generallycloud.baseio.container.FixedSession;
import com.generallycloud.baseio.container.SimpleIOEventHandle;
import com.generallycloud.baseio.container.jms.TextByteMessage;
import com.generallycloud.baseio.container.jms.client.MessageProducer;
import com.generallycloud.baseio.container.jms.client.impl.DefaultMessageProducer;
import com.generallycloud.test.nio.common.IoConnectorUtil;

public class TestTellerByteMessage {

	public static void main(String[] args) throws Exception {
		
		SharedBundle.instance().loadAllProperties("nio");

		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		SocketChannelConnector connector = IoConnectorUtil.getTCPConnector(eventHandle);

		FixedSession session = new FixedSession(connector.connect());

		session.login("admin", "admin100");

		MessageProducer producer = new DefaultMessageProducer(session);
		
		TextByteMessage message = new TextByteMessage("msgID", "UUID", "============","你好！".getBytes(session.getContext().getEncoding()));

		long old = System.currentTimeMillis();
		
		producer.offer(message);
		
		producer.offer(message);
		
		System.out.println("Time:" + (System.currentTimeMillis() - old));

		connector.close();

	}

}
