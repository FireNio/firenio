package test.jms;

import test.ClientUtil;

import com.gifisan.nio.client.ClientTCPConnector;
import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.plugin.jms.Message;
import com.gifisan.nio.plugin.jms.client.MessageConsumer;
import com.gifisan.nio.plugin.jms.client.OnMessage;
import com.gifisan.nio.plugin.jms.client.impl.DefaultMessageConsumer;

public class TestListenerPower {

	public static void main(String[] args) throws Exception {

		ClientTCPConnector connector = ClientUtil.getClientConnector();
		
		connector.connect();
		
		connector.login("admin", "admin100");
		
		ClientSession session = connector.getClientSession();
		
		MessageConsumer consumer = new DefaultMessageConsumer(session);
		long old = System.currentTimeMillis();
		
		OnMessage onMessage = new OnMessage() {
			
			public void onReceive(Message message) {
				System.out.println(message);
			}
		};
		
		
		for (int i = 0; i < 10000; i++) {
			consumer.receive(onMessage);
		}

		System.out.println("Time:" + (System.currentTimeMillis() - old));

		connector.close();
	}
}
