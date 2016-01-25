package test.jms;

import java.io.IOException;

import test.ClientUtil;

import com.gifisan.mtp.client.ClientConnector;
import com.gifisan.mtp.client.ClientSesssion;
import com.gifisan.mtp.jms.JMSException;
import com.gifisan.mtp.jms.Message;
import com.gifisan.mtp.jms.client.MessageConsumer;
import com.gifisan.mtp.jms.client.impl.MessageConsumerImpl;

public class TestListener {

	public static void main(String[] args) throws IOException, JMSException {
		
		ClientConnector connector = ClientUtil.getClientConnector();
		
		connector.connect();
		
		ClientSesssion session = connector.getClientSession();
		
		MessageConsumer consumer = new MessageConsumerImpl(session, "sssssss");

		consumer.login("admin", "admin100");
		
		long old = System.currentTimeMillis();

		Message message = consumer.revice();

		System.out.println("Time:" + (System.currentTimeMillis() - old));
		System.out.println(message);

		consumer.logout();
		
		connector.close();

	}

}
