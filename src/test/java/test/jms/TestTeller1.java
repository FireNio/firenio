package test.jms;

import com.yoocent.mtp.jms.JMSException;
import com.yoocent.mtp.jms.client.MessageProducer;
import com.yoocent.mtp.jms.client.impl.MessageProducerImpl;
import com.yoocent.mtp.jms.client.impl.TextMessage;

public class TestTeller1 {

	
	
	
	public static void main(String[] args) throws JMSException {
		
		MessageProducer producer = new MessageProducerImpl("mtp://localhost:8080", "admin", "admin100", TestTeller1.class.getName());
		
		producer.connect();
		
		TextMessage message = new TextMessage("wwww","sssssss", "tttttttttttttt");
		
		producer.send(message);
		
		producer.disconnect();
		
	}
}
