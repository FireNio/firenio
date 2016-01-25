package test.jms;

import test.ClientUtil;

import com.gifisan.mtp.client.ClientConnector;
import com.gifisan.mtp.client.ClientSesssion;
import com.gifisan.mtp.jms.Message;
import com.gifisan.mtp.jms.client.MessageConsumer;
import com.gifisan.mtp.jms.client.impl.MessageConsumerImpl;

public class TestListener1 {

	public static void main(String[] args) throws Exception {

		ClientConnector connector = ClientUtil.getClientConnector();
		
		connector.connect();
		
		ClientSesssion session = connector.getClientSession();
		
		MessageConsumer consumer = new MessageConsumerImpl(session, "sssssss");

		consumer.login("admin", "admin100");
		long old = System.currentTimeMillis();
		for (int i = 0; i < 20000; i++) {
			Message message = consumer.revice();
			System.out.println(message);
		}

		System.out.println("Time:" + (System.currentTimeMillis() - old));

		consumer.logout();
		
		connector.close();
	}
}
