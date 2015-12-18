package test.jms;

import com.gifisan.mtp.jms.JMSException;
import com.gifisan.mtp.jms.Message;
import com.gifisan.mtp.jms.MessageConsumer;
import com.gifisan.mtp.jms.client.impl.MessageConsumerImpl;

public class TestListener1 {

	
	
	public static void main(String[] args) throws JMSException {
		
		
		MessageConsumer consumer = new MessageConsumerImpl
				("mtp://localhost:8300", TestListener1.class.getName(),"sssssss",0);
		
		
		consumer.connect("admin", "admin100");
		long old = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			
			
			Message message = consumer.revice();
		}
		
		System.out.println("Time:"+(System.currentTimeMillis() - old));
//		System.out.println(message);
		
		consumer.disconnect();
	}
}
