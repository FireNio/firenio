package test.jms;

import test.ClientUtil;

import com.gifisan.nio.client.ClientConnector;
import com.gifisan.nio.client.ClientSesssion;
import com.gifisan.nio.jms.TextMessage;
import com.gifisan.nio.jms.client.MessageProducer;
import com.gifisan.nio.jms.client.impl.MessageProducerImpl;

public class TestTellerPower {

	public static void main(String[] args) throws Exception {

		ClientConnector connector = ClientUtil.getClientConnector();

		connector.connect();

		ClientSesssion session = connector.getClientSession();

		MessageProducer producer = new MessageProducerImpl(session);

		producer.login("admin", "admin100");

		TextMessage message = new TextMessage("wwww", "sssssss", "tttttttttttttt");

		long old = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			producer.offer(message);

		}
		System.out.println("Time:" + (System.currentTimeMillis() - old));

		producer.logout();

		connector.close();

	}
}
