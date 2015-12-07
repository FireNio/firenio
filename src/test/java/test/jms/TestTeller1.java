package test.jms;

import com.yoocent.mtp.jms.JMSException;
import com.yoocent.mtp.jms.MessageProducer;
import com.yoocent.mtp.jms.TextMessage;
import com.yoocent.mtp.jms.client.impl.MessageProducerImpl;

public class TestTeller1 {

	
	
	
	public static void main(String[] args) throws JMSException {
		
		MessageProducer producer = new MessageProducerImpl("mtp://localhost:8080",TestTeller1.class.getName());
		
		producer.connect( "admin", "admin100");
		
		TextMessage message = new TextMessage("wwww","sssssss", "tttttttttttttt");
		
		long old = System.currentTimeMillis();
		for (int i = 0; i < 8000; i++) {
			producer.send(message);
			
		}
		System.out.println("Time:"+(System.currentTimeMillis() - old));
		
		producer.disconnect();
		
	}
}
