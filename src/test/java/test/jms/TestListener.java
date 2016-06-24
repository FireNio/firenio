package test.jms;

import java.io.IOException;

import test.ClientUtil;

import com.gifisan.nio.client.TCPConnector;
import com.gifisan.nio.client.ConnectorSession;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.plugin.jms.JMSException;
import com.gifisan.nio.plugin.jms.Message;
import com.gifisan.nio.plugin.jms.client.MessageConsumer;
import com.gifisan.nio.plugin.jms.client.OnMessage;
import com.gifisan.nio.plugin.jms.client.impl.DefaultMessageConsumer;

public class TestListener {

	public static void main(String[] args) {
		
		TCPConnector connector = null;
		
		ConnectorSession session = null;
		try {
			
			connector = ClientUtil.getClientConnector();
			
			connector.connect();

//			connector.login("admin", "admin100");
			
			session = connector.getClientSession();
			
			MessageConsumer consumer = new DefaultMessageConsumer(session);

			long old = System.currentTimeMillis();

			consumer.receive(new OnMessage() {
				
				public void onReceive(Message message) {
					System.out.println(message);
					
				}
			});

			System.out.println("Time:" + (System.currentTimeMillis() - old));

			ThreadUtil.sleep(3500);

		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			CloseUtil.close(connector);
		}

	}

}
