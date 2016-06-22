package test.jms;

import java.io.IOException;

import test.ClientUtil;

import com.gifisan.nio.client.TCPConnector;
import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.plugin.jms.JMSException;
import com.gifisan.nio.plugin.jms.TextMessage;
import com.gifisan.nio.plugin.jms.client.MessageProducer;
import com.gifisan.nio.plugin.jms.client.impl.DefaultMessageProducer;

public class TestPublish {

	public static void main(String[] args) throws IOException, JMSException {

		TCPConnector connector = ClientUtil.getClientConnector();
		
		connector.connect();
		connector.login("admin", "admin100");

		ClientSession session = connector.getClientSession();

		MessageProducer producer = new DefaultMessageProducer(session);

		TextMessage message = new TextMessage("msgID", "qName", "你好！");

		producer.publish(message);

		connector.close();

	}

}
