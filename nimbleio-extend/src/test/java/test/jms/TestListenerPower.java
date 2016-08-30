package test.jms;

import com.generallycloud.nio.connector.TCPConnector;
import com.generallycloud.nio.extend.FixedSession;
import com.generallycloud.nio.extend.IOConnectorUtil;
import com.generallycloud.nio.extend.SimpleIOEventHandle;
import com.generallycloud.nio.extend.plugin.jms.Message;
import com.generallycloud.nio.extend.plugin.jms.client.MessageConsumer;
import com.generallycloud.nio.extend.plugin.jms.client.OnMessage;
import com.generallycloud.nio.extend.plugin.jms.client.impl.DefaultMessageConsumer;

public class TestListenerPower {

	public static void main(String[] args) throws Exception {

		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		TCPConnector connector = IOConnectorUtil.getTCPConnector(eventHandle);

		FixedSession session = eventHandle.getFixedSession();

		connector.connect();

		session.login("admin", "admin100");
		
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
