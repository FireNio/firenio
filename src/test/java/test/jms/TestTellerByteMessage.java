package test.jms;

import java.io.IOException;

import com.gifisan.nio.Encoding;
import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.extend.ClientLauncher;
import com.gifisan.nio.extend.FixedSession;
import com.gifisan.nio.plugin.jms.MQException;
import com.gifisan.nio.plugin.jms.TextByteMessage;
import com.gifisan.nio.plugin.jms.client.MessageProducer;
import com.gifisan.nio.plugin.jms.client.impl.DefaultMessageProducer;

public class TestTellerByteMessage {

	public static void main(String[] args) throws IOException, MQException {

		ClientLauncher launcher = new ClientLauncher();
		
		TCPConnector connector = launcher.getTCPConnector();

		connector.connect();
		
		FixedSession session = launcher.getFixedSession();

		session.login("admin", "admin100");

		MessageProducer producer = new DefaultMessageProducer(session);
		
		TextByteMessage message = new TextByteMessage("msgID", "UUID", "============","你好！".getBytes(Encoding.DEFAULT));

		long old = System.currentTimeMillis();
		
		producer.offer(message);
		
		producer.offer(message);
		
		System.out.println("Time:" + (System.currentTimeMillis() - old));

		connector.close();

	}

}
