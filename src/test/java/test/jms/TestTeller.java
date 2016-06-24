package test.jms;

import java.io.IOException;

import test.ClientUtil;

import com.gifisan.nio.client.TCPConnector;
import com.gifisan.nio.client.ConnectorSession;
import com.gifisan.nio.plugin.jms.JMSException;
import com.gifisan.nio.plugin.jms.MapMessage;
import com.gifisan.nio.plugin.jms.TextMessage;
import com.gifisan.nio.plugin.jms.client.MessageProducer;
import com.gifisan.nio.plugin.jms.client.impl.DefaultMessageProducer;

public class TestTeller {

	public static void main(String[] args) throws IOException, JMSException {

		TCPConnector connector = ClientUtil.getClientConnector();
		
		connector.connect();
		
		connector.login("admin", "admin100");

		ConnectorSession session = connector.getClientSession();

		MessageProducer producer = new DefaultMessageProducer(session);

		TextMessage message = new TextMessage("msgID", "UUID", "你好！");
		
		MapMessage mapMessage = new MapMessage("msgID", "qName");
		
		mapMessage.put("test","test111111111111111111111");

		long old = System.currentTimeMillis();
		
		producer.offer(message);
		
		producer.offer(mapMessage);

		System.out.println("Time:" + (System.currentTimeMillis() - old));

		connector.close();

	}

}
