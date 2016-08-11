package test.jms;

import java.io.IOException;

import test.IOConnectorUtil;

import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.extend.FixedSession;
import com.gifisan.nio.extend.SimpleIOEventHandle;
import com.gifisan.nio.extend.plugin.jms.MQException;
import com.gifisan.nio.extend.plugin.jms.Message;
import com.gifisan.nio.extend.plugin.jms.client.MessageConsumer;
import com.gifisan.nio.extend.plugin.jms.client.OnMessage;
import com.gifisan.nio.extend.plugin.jms.client.impl.DefaultMessageConsumer;

public class TestSubscribe {

	public static void main(String[] args) throws IOException, MQException {

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

		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		TCPConnector connector = IOConnectorUtil.getTCPConnector(eventHandle);

		FixedSession session = eventHandle.getFixedSession();

		connector.connect();

		session.login("admin", "admin100");

		MessageConsumer consumer = new DefaultMessageConsumer(session);

		consumer.subscribe(new OnMessage() {

			public void onReceive(Message message) {
				System.out.println(message);
			}
		});

		connector.close();
	}

}
