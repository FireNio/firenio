package test.jms;

import java.io.IOException;

import com.gifisan.mtp.jms.JMSException;
import com.gifisan.mtp.jms.Message;
import com.gifisan.mtp.jms.MessageConsumer;
import com.gifisan.mtp.jms.client.impl.MessageConsumerImpl;

public class TestListener {

	public static void main(String[] args) throws IOException, JMSException {
		MessageConsumer consumer = new MessageConsumerImpl("mtp://localhost:8300", "sssssss");

		consumer.connect("admin", "admin100");
		long old = System.currentTimeMillis();

		Message message = consumer.revice();

		System.out.println("Time:" + (System.currentTimeMillis() - old));
		System.out.println(message);

		consumer.disconnect();

	}

}
