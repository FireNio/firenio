package test.jms;

import java.io.IOException;

import test.ClientUtil;

import com.gifisan.nio.client.ClientTCPConnector;
import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.plugin.jms.JMSException;
import com.gifisan.nio.plugin.jms.Message;
import com.gifisan.nio.plugin.jms.client.MessageConsumer;
import com.gifisan.nio.plugin.jms.client.OnMessage;
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

		connector.login("admin", "admin100");
		
		ClientSession session = connector.getClientSession();

		MessageConsumer consumer = new DefaultMessageConsumer(session);

		consumer.subscribe(new OnMessage() {

			public void onReceive(Message message) {
				System.out.println(message);
			}
		});

		connector.close();
	}

}
