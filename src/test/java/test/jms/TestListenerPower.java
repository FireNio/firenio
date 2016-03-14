package test.jms;

import test.ClientUtil;

import com.gifisan.nio.client.ClientConnector;
import com.gifisan.nio.client.ClientSesssion;
import com.gifisan.nio.jms.Message;
import com.gifisan.nio.jms.client.MessageConsumer;
import com.gifisan.nio.jms.client.impl.MessageConsumerImpl;

public class TestListenerPower {

	public static void main(String[] args) throws Exception {

		ClientConnector connector = ClientUtil.getClientConnector();
		
		connector.connect();
		
		ClientSesssion session = connector.getClientSession();
		
		MessageConsumer consumer = new MessageConsumerImpl(session, "sssssss");

		consumer.login("admin", "admin100");
		long old = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			Message message = consumer.revice();
//			System.out.println(message);
		}

		System.out.println("Time:" + (System.currentTimeMillis() - old));

		consumer.logout();
		
		connector.close();
	}
}
