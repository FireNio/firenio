package com.generallycloud.test.nio.jms;

import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.container.FixedSession;
import com.generallycloud.nio.container.SimpleIOEventHandle;
import com.generallycloud.nio.container.jms.MQException;
import com.generallycloud.nio.container.jms.Message;
import com.generallycloud.nio.container.jms.client.MessageConsumer;
import com.generallycloud.nio.container.jms.client.OnMessage;
import com.generallycloud.nio.container.jms.client.impl.DefaultMessageConsumer;
import com.generallycloud.test.nio.common.IoConnectorUtil;

public class TestTransaction {

	public static void main(String[] args) throws Exception {
		
		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		SocketChannelConnector connector = IoConnectorUtil.getTCPConnector(eventHandle);

		FixedSession session = new FixedSession(connector.connect());
		
		session.login("admin", "admin100");
		
		MessageConsumer consumer = new DefaultMessageConsumer(session);
		
		rollback(consumer);
		
//		commit(consumer);

		connector.close();

	}
	
	
	static void commit(MessageConsumer consumer) throws MQException{
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
	
	static void rollback(MessageConsumer consumer) throws MQException{
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
