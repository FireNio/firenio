package test.jms;

import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.extend.ClientLauncher;
import com.gifisan.nio.extend.FixedSession;
import com.gifisan.nio.extend.plugin.jms.Message;
import com.gifisan.nio.extend.plugin.jms.client.MessageBrowser;
import com.gifisan.nio.extend.plugin.jms.client.impl.DefaultMessageBrowser;

public class TestBrowser {

	public static void main(String[] args) throws Exception {
		
		String queueName = "qName";

		ClientLauncher launcher = new ClientLauncher();
		
		TCPConnector connector = launcher.getTCPConnector();

		connector.connect();
		
		FixedSession session = launcher.getFixedSession();

		session.login("admin", "admin100");

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
