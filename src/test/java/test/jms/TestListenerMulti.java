package test.jms;

import com.gifisan.mtp.jms.JMSException;
import com.gifisan.mtp.jms.Message;
import com.gifisan.mtp.jms.MessageConsumer;
import com.gifisan.mtp.jms.client.impl.MessageConsumerImpl;

public class TestListenerMulti {

	static int i = 0;
	
	static synchronized int ipp(){
		return i++;
		
	}
	
	public static void main(String[] args) throws JMSException {
		for (int j = 0; j < 500;j++ ) {
			new Thread(new Runnable() {
				public void run() {
					try {
						MessageConsumer consumer = new MessageConsumerImpl
								("mtp://localhost:8300", TestListenerMulti.class.getName()+"-"+ipp(),"sssssss",0);
						consumer.connect("admin", "admin100");
						Message message = consumer.revice();
						System.out.println(message);
						consumer.disconnect();
					} catch (JMSException e) {
						e.printStackTrace();
					}
					
				}
			}).start();
			
			
			
		}
		
		
		
	}
}
