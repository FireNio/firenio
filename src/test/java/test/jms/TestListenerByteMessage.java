package test.jms;

import java.io.IOException;

import test.ClientUtil;

import com.gifisan.nio.Encoding;
import com.gifisan.nio.client.ClientTCPConnector;
import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.plugin.jms.ByteMessage;
import com.gifisan.nio.plugin.jms.JMSException;
import com.gifisan.nio.plugin.jms.Message;
import com.gifisan.nio.plugin.jms.client.MessageConsumer;
import com.gifisan.nio.plugin.jms.client.OnMessage;
import com.gifisan.nio.plugin.jms.client.impl.DefaultMessageConsumer;

public class TestListenerByteMessage {

	public static void main(String[] args) throws IOException, JMSException {
		
		ClientTCPConnector connector = ClientUtil.getClientConnector();
		
		connector.connect();
		
		ClientSession session = connector.getClientSession();
		
		MessageConsumer consumer = new DefaultMessageConsumer(session, "qName");

		consumer.login("admin", "admin100");
		
		final long old = System.currentTimeMillis();

		consumer.receive(new OnMessage() {
			
			public void onReceive(Message message) {
				System.out.println(message);
				if (message.getMsgType() == Message.TYPE_BYTE) {
					ByteMessage _Message = (ByteMessage) message;
					System.out.println(new String(_Message.getByteArray(),Encoding.DEFAULT));
				}
				
				System.out.println("Time:" + (System.currentTimeMillis() - old));
			}
		});

		consumer.logout();
		
		connector.close();

	}

}
