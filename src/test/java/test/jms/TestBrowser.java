package test.jms;

import test.ClientUtil;

import com.gifisan.nio.client.TCPConnector;
import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.plugin.jms.Message;
import com.gifisan.nio.plugin.jms.client.MessageBrowser;
import com.gifisan.nio.plugin.jms.client.impl.DefaultMessageBrowser;

public class TestBrowser {

	public static void main(String[] args) throws Exception {
		
		String queueName = "qName";

		TCPConnector connector = ClientUtil.getClientConnector();

		connector.connect();
		
		connector.login("admin", "admin100");

		ClientSession session = connector.getClientSession();

		MessageBrowser browser = new DefaultMessageBrowser(session);

		Message message = browser.browser(queueName);
		
		System.out.println("message:"+message);
		
		int size = browser.size();
		
		System.out.println("size:"+size);
		
		boolean isOnline = browser.isOnline(queueName);
		
		System.out.println("isOnline:"+isOnline);

		connector.close();

	}
}
