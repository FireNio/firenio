package test.jms;

import java.io.IOException;

import test.ClientUtil;

import com.gifisan.nio.client.ClientConnector;
import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.jms.JMSException;
import com.gifisan.nio.jms.Message;
import com.gifisan.nio.jms.client.MessageConsumer;
import com.gifisan.nio.jms.client.impl.MessageConsumerImpl;

public class TestTransaction {

	public static void main(String[] args) throws IOException, JMSException {
		
		ClientConnector connector = ClientUtil.getClientConnector();
		
		connector.connect();
		
		ClientSession session = connector.getClientSession();
		
		MessageConsumer consumer = new MessageConsumerImpl(session, "qName");

		consumer.login("admin", "admin100");
		
		rollback(consumer);
		
//		commit(consumer);

		consumer.logout();
		
		connector.close();

	}
	
	
	static void commit(MessageConsumer consumer) throws JMSException{
		long old = System.currentTimeMillis();

		consumer.beginTransaction();
		
		Message message = consumer.revice();

		consumer.commit();
		
		System.out.println("Time:" + (System.currentTimeMillis() - old));
		System.out.println(message);
	}
	
	static void rollback(MessageConsumer consumer) throws JMSException{
		long old = System.currentTimeMillis();

		consumer.beginTransaction();
		
		Message message = consumer.revice();

		consumer.rollback();
		
		System.out.println("Time:" + (System.currentTimeMillis() - old));
		System.out.println(message);
		
	}

}
