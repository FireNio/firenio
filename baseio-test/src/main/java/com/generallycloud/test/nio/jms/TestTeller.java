package com.generallycloud.test.nio.jms;

import com.generallycloud.nio.codec.protobuf.ProtobufProtocolFactory;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.container.FixedSession;
import com.generallycloud.nio.container.SimpleIOEventHandle;
import com.generallycloud.nio.container.jms.MapMessage;
import com.generallycloud.nio.container.jms.TextMessage;
import com.generallycloud.nio.container.jms.client.MessageProducer;
import com.generallycloud.nio.container.jms.client.impl.DefaultMessageProducer;
import com.generallycloud.test.nio.common.IoConnectorUtil;

public class TestTeller {

	public static void main(String[] args) throws Exception {
		
		SharedBundle.instance().loadAllProperties("nio");

		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		SocketChannelConnector connector = IoConnectorUtil.getTCPConnector(eventHandle);
		
		connector.getContext().setProtocolFactory(new ProtobufProtocolFactory());
		
		FixedSession session = new FixedSession(connector.connect());

		boolean b = session.login("admin", "admin100");
		
		System.out.println(b);

		MessageProducer producer = new DefaultMessageProducer(session);

		TextMessage message = new TextMessage("msgID", "uuid", "你好！");
		
		MapMessage mapMessage = new MapMessage("msgID", "uuid");
		
		mapMessage.put("test","test111111111111111111111");

		long old = System.currentTimeMillis();
		
		producer.offer(message);
		
		producer.offer(mapMessage);

		System.out.println("Time:" + (System.currentTimeMillis() - old));

		connector.close();

	}

}
