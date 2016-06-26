package test.jms;

import java.io.IOException;

import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.extend.ClientLauncher;
import com.gifisan.nio.extend.FixedSession;
import com.gifisan.nio.plugin.jms.MQException;
import com.gifisan.nio.plugin.jms.MapMessage;
import com.gifisan.nio.plugin.jms.TextMessage;
import com.gifisan.nio.plugin.jms.client.MessageProducer;
import com.gifisan.nio.plugin.jms.client.impl.DefaultMessageProducer;

public class TestTeller {

	public static void main(String[] args) throws IOException, MQException {

		ClientLauncher launcher = new ClientLauncher();
		
		TCPConnector connector = launcher.getTCPConnector();

		connector.connect();
		
		FixedSession session = launcher.getFixedSession();

		session.login("admin", "admin100");

		MessageProducer producer = new DefaultMessageProducer(session);

		TextMessage message = new TextMessage("msgID", "UUID", "你好！");
		
		MapMessage mapMessage = new MapMessage("msgID", "UUID");
		
		mapMessage.put("test","test111111111111111111111");

		long old = System.currentTimeMillis();
		
		producer.offer(message);
		
		producer.offer(mapMessage);

		System.out.println("Time:" + (System.currentTimeMillis() - old));

		connector.close();

	}

}
