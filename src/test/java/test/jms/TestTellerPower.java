package test.jms;

import test.ClientUtil;

import com.gifisan.nio.client.ClientTCPConnector;
import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.plugin.jms.TextMessage;
import com.gifisan.nio.plugin.jms.client.MessageProducer;
import com.gifisan.nio.plugin.jms.client.impl.DefaultMessageProducer;

public class TestTellerPower {

	public static void main(String[] args) throws Exception {

		ClientTCPConnector connector = ClientUtil.getClientConnector();

		connector.connect();

		ClientSession session = connector.getClientSession();

		MessageProducer producer = new DefaultMessageProducer(session);

		producer.login("admin", "admin100");

		TextMessage message = new TextMessage("msgID", "qName", "你好！");

		long old = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			producer.offer(message);

		}
		System.out.println("Time:" + (System.currentTimeMillis() - old));

		producer.logout();

		connector.close();

	}
}
