package test.jms;

import java.io.IOException;

import com.gifisan.nio.common.IOConnectorUtil;
import com.gifisan.nio.common.PropertiesLoader;
import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.extend.FixedSession;
import com.gifisan.nio.extend.SimpleIOEventHandle;
import com.gifisan.nio.extend.plugin.jms.MQException;
import com.gifisan.nio.extend.plugin.jms.MapMessage;
import com.gifisan.nio.extend.plugin.jms.TextMessage;
import com.gifisan.nio.extend.plugin.jms.client.MessageProducer;
import com.gifisan.nio.extend.plugin.jms.client.impl.DefaultMessageProducer;

public class TestTeller {

	public static void main(String[] args) throws IOException, MQException {
		
		PropertiesLoader.setBasepath("nio");

		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		TCPConnector connector = IOConnectorUtil.getTCPConnector(eventHandle);
		
		FixedSession session = eventHandle.getFixedSession();

		connector.connect();

		boolean b = session.login("wk", "wk");
		
		System.out.println(b);

		MessageProducer producer = new DefaultMessageProducer(session);

		TextMessage message = new TextMessage("msgID", "ea36e428892a4e5b9e5f7c0ba40226ac", "你好！");
		
		MapMessage mapMessage = new MapMessage("msgID", "ea36e428892a4e5b9e5f7c0ba40226ac");
		
		mapMessage.put("test","test111111111111111111111");

		long old = System.currentTimeMillis();
		
		producer.offer(message);
		
		producer.offer(mapMessage);

		System.out.println("Time:" + (System.currentTimeMillis() - old));

		connector.close();

	}

}
