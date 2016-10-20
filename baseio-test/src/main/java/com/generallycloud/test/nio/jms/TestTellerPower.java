package com.generallycloud.test.nio.jms;

import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.extend.FixedSession;
import com.generallycloud.nio.extend.IOConnectorUtil;
import com.generallycloud.nio.extend.SimpleIOEventHandle;
import com.generallycloud.nio.extend.plugin.jms.TextMessage;
import com.generallycloud.nio.extend.plugin.jms.client.MessageProducer;
import com.generallycloud.nio.extend.plugin.jms.client.impl.DefaultMessageProducer;

public class TestTellerPower {

	public static void main(String[] args) throws Exception {

		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		SocketChannelConnector connector = IOConnectorUtil.getTCPConnector(eventHandle);

		FixedSession session = eventHandle.getFixedSession();

		connector.connect();

		session.login("admin", "admin100");

		MessageProducer producer = new DefaultMessageProducer(session);

		TextMessage message = new TextMessage("msgID", "qName", "你好！");

		long old = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			producer.offer(message);

		}
		System.out.println("Time:" + (System.currentTimeMillis() - old));

		connector.close();

	}
}
