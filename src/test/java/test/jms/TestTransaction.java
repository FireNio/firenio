package test.jms;

import java.io.IOException;

import com.gifisan.mtp.jms.JMSException;
import com.gifisan.mtp.jms.Message;
import com.gifisan.mtp.jms.MessageConsumer;
import com.gifisan.mtp.jms.client.impl.MessageConsumerImpl;

public class TestTransaction {

	public static void main(String[] args) throws IOException, JMSException {
		MessageConsumer consumer = new MessageConsumerImpl("mtp://localhost:8300", TestListener1.class.getName(),
				"sssssss", 0);
		
		rollback(consumer);
		
//		commit(consumer);

		consumer.disconnect();

	}
	
	
	static void commit(MessageConsumer consumer) throws JMSException{
		consumer.connect("admin", "admin100");
		long old = System.currentTimeMillis();

		consumer.beginTransaction();
		
		Message message = consumer.revice();

		consumer.commit();
		
		System.out.println("Time:" + (System.currentTimeMillis() - old));
		System.out.println(message);
	}
	
	static void rollback(MessageConsumer consumer) throws JMSException{
		consumer.connect("admin", "admin100");
		long old = System.currentTimeMillis();

		consumer.beginTransaction();
		
		Message message = consumer.revice();

		consumer.rollback();
		
		System.out.println("Time:" + (System.currentTimeMillis() - old));
		System.out.println(message);
		
	}

}
