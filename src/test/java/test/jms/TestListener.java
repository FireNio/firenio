package test.jms;

import java.io.IOException;

import test.ClientUtil;

import com.gifisan.nio.client.ClientConnector;
import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.jms.JMSException;
import com.gifisan.nio.jms.Message;
import com.gifisan.nio.jms.client.MessageConsumer;
import com.gifisan.nio.jms.client.impl.MessageConsumerImpl;

public class TestListener {

	public static void main(String[] args) {
		
		ClientConnector connector = null;
		
		ClientSession session = null;
		try {
			
			connector = ClientUtil.getClientConnector();
			
			connector.connect();
			
			session = connector.getClientSession();
			
			MessageConsumer consumer = new MessageConsumerImpl(session, "qName");

			consumer.login("admin", "admin1001");
			
			long old = System.currentTimeMillis();

			Message message = consumer.receive();

			System.out.println("Time:" + (System.currentTimeMillis() - old));
			System.out.println(message);

			consumer.logout();
			
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JMSException e) {
			e.printStackTrace();
		}finally{
			CloseUtil.close(connector);
		}

	}

}
