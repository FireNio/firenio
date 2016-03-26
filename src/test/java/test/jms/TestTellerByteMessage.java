package test.jms;

import java.io.IOException;

import test.ClientUtil;

import com.gifisan.nio.Encoding;
import com.gifisan.nio.client.ClientConnector;
import com.gifisan.nio.client.ClientSesssion;
import com.gifisan.nio.jms.ByteMessage;
import com.gifisan.nio.jms.JMSException;
import com.gifisan.nio.jms.client.MessageProducer;
import com.gifisan.nio.jms.client.impl.MessageProducerImpl;

public class TestTellerByteMessage {

	public static void main(String[] args) throws IOException, JMSException {

		ClientConnector connector = ClientUtil.getClientConnector();
		
		connector.connect();
		
		ClientSesssion session = connector.getClientSession();
		
		MessageProducer producer = new MessageProducerImpl(session);

		producer.login("admin", "admin100");

		ByteMessage message = new ByteMessage("wwww", "舒华", "你好，舒华".getBytes(Encoding.DEFAULT));

		long old = System.currentTimeMillis();
		producer.offer(message);

		System.out.println("Time:" + (System.currentTimeMillis() - old));

		producer.logout();
		
		connector.close();

	}

}
