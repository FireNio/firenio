package test.jms;

import com.gifisan.mtp.jms.JMSException;
import com.gifisan.mtp.jms.MessageProducer;
import com.gifisan.mtp.jms.TextMessage;
import com.gifisan.mtp.jms.client.impl.MessageProducerImpl;

public class TestTeller1 {

	
	
	
	public static void main(String[] args) throws JMSException {
		
		MessageProducer producer = new MessageProducerImpl("mtp://localhost:8080",TestTeller1.class.getName());
		
		producer.connect( "admin", "admin100");
		
		TextMessage message = new TextMessage("wwww","sssssss", "tttttttttttttt");
		
		long old = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			producer.send(message);
			
		}
		System.out.println("Time:"+(System.currentTimeMillis() - old));
		
		producer.disconnect();
		
	}
}
