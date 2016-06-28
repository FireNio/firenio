package test.jms;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.extend.ClientLauncher;
import com.gifisan.nio.extend.FixedSession;
import com.gifisan.nio.extend.plugin.jms.Message;
import com.gifisan.nio.extend.plugin.jms.client.MessageConsumer;
import com.gifisan.nio.extend.plugin.jms.client.OnMessage;
import com.gifisan.nio.extend.plugin.jms.client.impl.DefaultMessageConsumer;

public class TestListener {

	public static void main(String[] args) throws Exception {

		ClientLauncher launcher = new ClientLauncher();

		TCPConnector connector = launcher.getTCPConnector();

		connector.connect();

		FixedSession session = launcher.getFixedSession();

		session.login("admin", "admin100");

		MessageConsumer consumer = new DefaultMessageConsumer(session);

		long old = System.currentTimeMillis();

		consumer.receive(new OnMessage() {

			public void onReceive(Message message) {
				System.out.println(message);
			}
		});

		System.out.println("Time:" + (System.currentTimeMillis() - old));

		ThreadUtil.sleep(500);
		
		CloseUtil.close(connector);

	}

}
