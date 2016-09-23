package com.generallycloud.test.nio.jms;

import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.extend.FixedSession;
import com.generallycloud.nio.extend.IOConnectorUtil;
import com.generallycloud.nio.extend.SimpleIOEventHandle;
import com.generallycloud.nio.extend.plugin.jms.MQException;
import com.generallycloud.nio.extend.plugin.jms.Message;
import com.generallycloud.nio.extend.plugin.jms.client.MessageConsumer;
import com.generallycloud.nio.extend.plugin.jms.client.OnMessage;
import com.generallycloud.nio.extend.plugin.jms.client.impl.DefaultMessageConsumer;

public class TestTransaction {

	public static void main(String[] args) throws Exception {
		
		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		SocketChannelConnector connector = IOConnectorUtil.getTCPConnector(eventHandle);

		FixedSession session = eventHandle.getFixedSession();

		connector.connect();
		
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
