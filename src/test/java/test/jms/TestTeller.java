package test.jms;

import java.io.IOException;

import com.gifisan.mtp.jms.JMSException;
import com.gifisan.mtp.jms.MessageProducer;
import com.gifisan.mtp.jms.TextMessage;
import com.gifisan.mtp.jms.client.impl.MessageProducerImpl;

public class TestTeller {

	public static void main(String[] args) throws IOException, JMSException {

		MessageProducer producer = new MessageProducerImpl("mtp://localhost:8300", TestTeller1.class.getName());

		producer.connect("admin", "admin100");

		TextMessage message = new TextMessage("wwww", "sssssss", "tttttttttttttt");

		long old = System.currentTimeMillis();
		producer.offer(message);

		System.out.println("Time:" + (System.currentTimeMillis() - old));

		producer.disconnect();

	}

}
