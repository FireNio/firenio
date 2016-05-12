package test.jms;

import java.io.IOException;

import test.ClientUtil;

import com.gifisan.nio.client.ClientTCPConnector;
import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.jms.JMSException;
import com.gifisan.nio.jms.Message;
import com.gifisan.nio.jms.client.MessageConsumer;
import com.gifisan.nio.jms.client.OnMessage;
import com.gifisan.nio.jms.client.impl.MessageConsumerImpl;

public class TestListenerCallBack {

	public static void main(String[] args) throws IOException, JMSException {
		
		final ClientTCPConnector connector = ClientUtil.getClientConnector();
		
		connector.connect();
		
		ClientSession session = connector.getClientSession();
		
		final MessageConsumer consumer = new MessageConsumerImpl(session, "qName");

		consumer.login("admin", "admin100");
		
		consumer.receive( new OnMessage() {
			
			public void onReceive(Message message) {
				System.out.println(message);
			}
		});

		
		ThreadUtil.sleep(1000);
		consumer.logout();
		CloseUtil.close(connector);
		

	}

}
