package test.jms;

import java.io.IOException;

import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.extend.ClientLauncher;
import com.gifisan.nio.extend.FixedSession;
import com.gifisan.nio.extend.plugin.jms.MQException;
import com.gifisan.nio.extend.plugin.jms.TextMessage;
import com.gifisan.nio.extend.plugin.jms.client.MessageProducer;
import com.gifisan.nio.extend.plugin.jms.client.impl.DefaultMessageProducer;

public class TestPublish {

	public static void main(String[] args) throws IOException, MQException {

		ClientLauncher launcher = new ClientLauncher();
		
		TCPConnector connector = launcher.getTCPConnector();

		connector.connect();
		
		FixedSession session = launcher.getFixedSession();

		session.login("admin", "admin100");

		MessageProducer producer = new DefaultMessageProducer(session);

		TextMessage message = new TextMessage("msgID", "qName", "你好！");

		producer.publish(message);

		connector.close();

	}

}
