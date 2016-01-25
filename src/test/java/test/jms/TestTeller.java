package test.jms;

import java.io.IOException;

import test.ClientUtil;

import com.gifisan.mtp.client.ClientConnector;
import com.gifisan.mtp.client.ClientSesssion;
import com.gifisan.mtp.jms.JMSException;
import com.gifisan.mtp.jms.TextMessage;
import com.gifisan.mtp.jms.client.MessageProducer;
import com.gifisan.mtp.jms.client.impl.MessageProducerImpl;

public class TestTeller {

	public static void main(String[] args) throws IOException, JMSException {

		ClientConnector connector = ClientUtil.getClientConnector();
		
		connector.connect();
		
		ClientSesssion session = connector.getClientSession();
		
		MessageProducer producer = new MessageProducerImpl(session);

		producer.login("admin", "admin100");

		TextMessage message = new TextMessage("wwww", "sssssss", "tttttttttttttt");

		long old = System.currentTimeMillis();
		producer.offer(message);

		System.out.println("Time:" + (System.currentTimeMillis() - old));

		producer.logout();
		
		connector.close();

	}

}
