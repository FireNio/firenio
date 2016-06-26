package test.jms;

import java.io.IOException;

import com.gifisan.nio.Encoding;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.extend.ClientLauncher;
import com.gifisan.nio.extend.FixedSession;
import com.gifisan.nio.plugin.jms.JMSException;
import com.gifisan.nio.plugin.jms.Message;
import com.gifisan.nio.plugin.jms.TextByteMessage;
import com.gifisan.nio.plugin.jms.client.MessageConsumer;
import com.gifisan.nio.plugin.jms.client.OnMessage;
import com.gifisan.nio.plugin.jms.client.impl.DefaultMessageConsumer;

public class TestListenerByteMessage {

	public static void main(String[] args) throws IOException, JMSException {
		
		ClientLauncher launcher = new ClientLauncher();
		
		TCPConnector connector = launcher.getTCPConnector();

		connector.connect();
		
		FixedSession session = launcher.getFixedSession();

		session.login("admin", "admin100");
		
		MessageConsumer consumer = new DefaultMessageConsumer(session);

		final long old = System.currentTimeMillis();

		consumer.receive(new OnMessage() {
			
			public void onReceive(Message message) {
				System.out.println(message);
				if (message.getMsgType() == Message.TYPE_TEXT_BYTE) {
					TextByteMessage _Message = (TextByteMessage) message;
					System.out.println(new String(_Message.getByteArray(),Encoding.DEFAULT));
				}
				
				System.out.println("Time:" + (System.currentTimeMillis() - old));
			}
		});
		
		ThreadUtil.sleep(1000);

		connector.close();

	}

}
