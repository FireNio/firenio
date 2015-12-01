package test.jms;

import com.yoocent.mtp.jms.JMSException;
import com.yoocent.mtp.jms.client.Message;
import com.yoocent.mtp.jms.client.MessageConsumer;
import com.yoocent.mtp.jms.client.impl.MessageConsumerImpl;

public class TestListener1 {

	
	
	public static void main(String[] args) throws JMSException {
		
		
		MessageConsumer consumer = new MessageConsumerImpl("mtp://localhost:8080", "admin", "admin100", TestTeller1.class.getName(),"sssssss",0);
		
		
		consumer.connect();
		
		Message message = consumer.revice();
		
		System.out.println(message);
		
		consumer.disconnect();
	}
}
