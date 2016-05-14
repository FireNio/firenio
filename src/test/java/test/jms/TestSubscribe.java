package test.jms;

import java.io.IOException;

import test.ClientUtil;

import com.gifisan.nio.client.ClientTCPConnector;
import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.plugin.jms.JMSException;
import com.gifisan.nio.plugin.jms.Message;
import com.gifisan.nio.plugin.jms.client.MessageConsumer;
import com.gifisan.nio.plugin.jms.client.impl.DefaultMessageConsumer;

public class TestSubscribe {

	public static void main(String[] args) throws IOException, JMSException {

		
		for (int i = 0; i < 5; i++) {
			
			new Thread(new Runnable() {
				
				public void run() {
					try {
						TestSubscribe.test();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
			
		}
		
	}

	private static void test() throws Exception {

		ClientTCPConnector connector = ClientUtil.getClientConnector();

		connector.connect();

		ClientSession session = connector.getClientSession();

		MessageConsumer consumer = new DefaultMessageConsumer(session, "qName");

		consumer.login("admin", "admin100");

		Message message = consumer.subscribe();

		System.out.println(message);

		consumer.logout();

		connector.close();
	}

}
