package test.jms;

import test.ClientUtil;

import com.gifisan.nio.client.ClientTCPConnector;
import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.jms.Message;
import com.gifisan.nio.jms.client.MessageBrowser;
import com.gifisan.nio.jms.client.impl.MessageBrowserImpl;

public class TestBrowser {

	public static void main(String[] args) throws Exception {
		
		String queueName = "qName";

		ClientTCPConnector connector = ClientUtil.getClientConnector();

		connector.connect();

		ClientSession session = connector.getClientSession();

		MessageBrowser browser = new MessageBrowserImpl(session);

		browser.login("admin", "admin100");

		Message message = browser.browser(queueName);
		
		System.out.println("message:"+message);
		
		int size = browser.size();
		
		System.out.println("size:"+size);
		
		boolean isOnline = browser.isOnline(queueName);
		
		System.out.println("isOnline:"+isOnline);

		browser.logout();

		connector.close();

	}
}
