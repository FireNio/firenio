package test.jms;

import com.gifisan.mtp.jms.JMSException;
import com.gifisan.mtp.jms.Message;
import com.gifisan.mtp.jms.MessageBrowser;
import com.gifisan.mtp.jms.client.impl.MessageBrowserImpl;

public class TestBrowser1 {

	public static void main(String[] args) throws JMSException {

		MessageBrowser browser = new MessageBrowserImpl("mtp://localhost:8300");

		browser.connect("admin", "admin100");

		Message message = browser.browser("wwww");

		System.out.println(message);

		browser.disconnect();

	}
}
