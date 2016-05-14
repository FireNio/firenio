package test.jms;

import test.ClientUtil;

import com.gifisan.nio.client.ClientTCPConnector;
import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.plugin.jms.Message;
import com.gifisan.nio.plugin.jms.client.MessageConsumer;
import com.gifisan.nio.plugin.jms.client.impl.DefaultMessageConsumer;

public class TestListenerPower {

	public static void main(String[] args) throws Exception {

		ClientTCPConnector connector = ClientUtil.getClientConnector();
		
		connector.connect();
		
		ClientSession session = connector.getClientSession();
		
		MessageConsumer consumer = new DefaultMessageConsumer(session, "qName");

		consumer.login("admin", "admin100");
		long old = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			Message message = consumer.receive();
			System.out.println(message);
		}

		System.out.println("Time:" + (System.currentTimeMillis() - old));

		consumer.logout();
		
		connector.close();
	}
}
