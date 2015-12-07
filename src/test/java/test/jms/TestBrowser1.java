package test.jms;

import com.yoocent.mtp.jms.JMSException;
import com.yoocent.mtp.jms.Message;
import com.yoocent.mtp.jms.MessageBrowser;
import com.yoocent.mtp.jms.client.impl.MessageBrowserImpl;

public class TestBrowser1 {

	
	
	
	public static void main(String[] args) throws JMSException {
		
		MessageBrowser browser = new MessageBrowserImpl
				("mtp://localhost:8080", TestBrowser1.class.getName());
		
		
		
		browser.connect("admin", "admin100");
		
		Message message = browser.browser("wwww");
		
		System.out.println(message);
		
		browser.disconnect();
		
	}
}
