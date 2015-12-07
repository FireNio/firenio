package test.jms;

import java.io.IOException;

import com.yoocent.mtp.jms.client.MessageConsumer;

public class TestListener {

	public static void main(String[] args) throws IOException {
		
		long timeout = 999100000;

		MessageConsumer consumer = new MessageConsumer();
		
		consumer.connect();
		
		long old = System.currentTimeMillis();
		
		for (int i = 0; i < 10000; i++) {
			String content = consumer.reveice("test-listener",timeout);
//			System.out.println(content+i);
			
		}
		
		System.out.println("Time:"+(System.currentTimeMillis() - old));
		consumer.close();
		
		//System.out.println(content);
		

	}

}
