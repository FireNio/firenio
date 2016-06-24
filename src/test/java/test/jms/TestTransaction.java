package test.jms;

import java.io.IOException;

import test.ClientUtil;

import com.gifisan.nio.client.TCPConnector;
import com.gifisan.nio.client.ConnectorSession;
import com.gifisan.nio.plugin.jms.JMSException;
import com.gifisan.nio.plugin.jms.Message;
import com.gifisan.nio.plugin.jms.client.MessageConsumer;
import com.gifisan.nio.plugin.jms.client.OnMessage;
import com.gifisan.nio.plugin.jms.client.impl.DefaultMessageConsumer;

public class TestTransaction {

	public static void main(String[] args) throws IOException, JMSException {
		
		TCPConnector connector = ClientUtil.getClientConnector();
		
		connector.connect();
		
		connector.login("admin", "admin100");
		
		ConnectorSession session = connector.getClientSession();
		
		MessageConsumer consumer = new DefaultMessageConsumer(session);
		
		rollback(consumer);
		
//		commit(consumer);

		connector.close();

	}
	
	
	static void commit(MessageConsumer consumer) throws JMSException{
		long old = System.currentTimeMillis();

		consumer.beginTransaction();
		
		consumer.receive(new OnMessage() {
			
			public void onReceive(Message message) {
				System.out.println(message);
			}
		});

		consumer.commit();
		
		System.out.println("Time:" + (System.currentTimeMillis() - old));
		
	}
	
	static void rollback(MessageConsumer consumer) throws JMSException{
		long old = System.currentTimeMillis();

		consumer.beginTransaction();
		
		consumer.receive(new OnMessage() {
			
			public void onReceive(Message message) {
				
				System.out.println(message);
			}
		});

		consumer.rollback();
		
		System.out.println("Time:" + (System.currentTimeMillis() - old));
	}

}
