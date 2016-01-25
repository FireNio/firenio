package test.jms;

import java.io.IOException;

import test.ClientUtil;

import com.gifisan.mtp.client.ClientConnector;
import com.gifisan.mtp.client.ClientSesssion;
import com.gifisan.mtp.jms.JMSException;
import com.gifisan.mtp.jms.Message;
import com.gifisan.mtp.jms.client.MessageConsumer;
import com.gifisan.mtp.jms.client.impl.MessageConsumerImpl;

public class TestTransaction {

	public static void main(String[] args) throws IOException, JMSException {
		
		ClientConnector connector = ClientUtil.getClientConnector();
		
		connector.connect();
		
		ClientSesssion session = connector.getClientSession();
		
		MessageConsumer consumer = new MessageConsumerImpl(session, "sssssss");

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
