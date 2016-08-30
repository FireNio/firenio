package test.jms;

import java.io.IOException;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.extend.FixedSession;
import com.gifisan.nio.extend.IOConnectorUtil;
import com.gifisan.nio.extend.SimpleIOEventHandle;
import com.gifisan.nio.extend.plugin.jms.MQException;
import com.gifisan.nio.extend.plugin.jms.Message;
import com.gifisan.nio.extend.plugin.jms.client.MessageConsumer;
import com.gifisan.nio.extend.plugin.jms.client.OnMessage;
import com.gifisan.nio.extend.plugin.jms.client.impl.DefaultMessageConsumer;

public class TestListenerCallBack {

	public static void main(String[] args) throws IOException, MQException {
		
		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		TCPConnector connector = IOConnectorUtil.getTCPConnector(eventHandle);

		FixedSession session = eventHandle.getFixedSession();

		connector.connect();

		session.login("admin", "admin100");
		
		MessageConsumer consumer = new DefaultMessageConsumer(session);

		consumer.receive( new OnMessage() {
			
			public void onReceive(Message message) {
				System.out.println(message);
			}
		});

		
		ThreadUtil.sleep(1000);
		CloseUtil.close(connector);
		

	}

}
