package test.jms;

import java.io.IOException;

import com.gifisan.nio.Encoding;
import com.gifisan.nio.common.IOConnectorUtil;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.extend.FixedSession;
import com.gifisan.nio.extend.SimpleIOEventHandle;
import com.gifisan.nio.extend.plugin.jms.MQException;
import com.gifisan.nio.extend.plugin.jms.Message;
import com.gifisan.nio.extend.plugin.jms.TextByteMessage;
import com.gifisan.nio.extend.plugin.jms.client.MessageConsumer;
import com.gifisan.nio.extend.plugin.jms.client.OnMessage;
import com.gifisan.nio.extend.plugin.jms.client.impl.DefaultMessageConsumer;

public class TestListenerByteMessage {

	public static void main(String[] args) throws IOException, MQException {
		
		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		TCPConnector connector = IOConnectorUtil.getTCPConnector(eventHandle);

		FixedSession session = eventHandle.getFixedSession();

		connector.connect();
		
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
		
		ThreadUtil.sleep(3000);

		connector.close();

	}

}
