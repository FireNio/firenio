package com.generallycloud.test.nio.jms;

import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.extend.FixedSession;
import com.generallycloud.nio.extend.SimpleIOEventHandle;
import com.generallycloud.nio.extend.plugin.jms.TextByteMessage;
import com.generallycloud.nio.extend.plugin.jms.client.MessageProducer;
import com.generallycloud.nio.extend.plugin.jms.client.impl.DefaultMessageProducer;
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
