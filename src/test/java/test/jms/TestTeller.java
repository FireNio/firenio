package test.jms;

import java.io.IOException;

import com.gifisan.mtp.jms.client.MessageProducer;

public class TestTeller {

	public static void main(String[] args) throws IOException {
		
		long timeout = 999100000;

		MessageProducer producer = new MessageProducer();
		
		producer.connect();
		
		for (int i = 0; i < 10000; i++) {
			producer.send("test-listener","######################",timeout);
			
		}
		
		producer.close();
		

	}

}
