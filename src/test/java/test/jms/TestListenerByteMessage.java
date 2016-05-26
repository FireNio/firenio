package test.jms;

import java.io.IOException;

import test.ClientUtil;

import com.gifisan.nio.Encoding;
import com.gifisan.nio.client.ClientTCPConnector;
import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.plugin.jms.TextByteMessage;
import com.gifisan.nio.plugin.jms.JMSException;
import com.gifisan.nio.plugin.jms.Message;
import com.gifisan.nio.plugin.jms.client.MessageConsumer;
import com.gifisan.nio.plugin.jms.client.OnMessage;
import com.gifisan.nio.plugin.jms.client.impl.DefaultMessageConsumer;

public class TestListenerByteMessage {

	public static void main(String[] args) throws IOException, JMSException {
		
		ClientTCPConnector connector = ClientUtil.getClientConnector();
		
		connector.connect();

		connector.login("admin", "admin100");
		
		ClientSession session = connector.getClientSession();
		
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
