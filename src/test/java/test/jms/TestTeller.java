package test.jms;

import java.io.IOException;
import java.util.Map;

import test.ClientUtil;

import com.gifisan.nio.client.ClientTCPConnector;
import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.plugin.jms.JMSException;
import com.gifisan.nio.plugin.jms.MapMessage;
import com.gifisan.nio.plugin.jms.TextMessage;
import com.gifisan.nio.plugin.jms.client.MessageProducer;
import com.gifisan.nio.plugin.jms.client.impl.DefaultMessageProducer;
import com.gifisan.nio.plugin.jms.decode.MapMessageDecoder;

public class TestTeller {

	public static void main(String[] args) throws IOException, JMSException {

		ClientTCPConnector connector = ClientUtil.getClientConnector();
		
		connector.connect();
		
		ClientSession session = connector.getClientSession();
		
		MessageProducer producer = new DefaultMessageProducer(session);

		producer.login("admin", "admin100");

		TextMessage message = new TextMessage("msgID", "qName", "你好！");
		
		MapMessage mapMessage = new MapMessage("msgID", "qName");
		
		mapMessage.put("test","test111111111111111111111");

		long old = System.currentTimeMillis();
		
		producer.offer(message);
		
		producer.offer(mapMessage);

		System.out.println("Time:" + (System.currentTimeMillis() - old));

		producer.logout();
		
		connector.close();

	}

}
