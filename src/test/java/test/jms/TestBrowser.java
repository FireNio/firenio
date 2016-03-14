package test.jms;

import test.ClientUtil;

import com.gifisan.nio.client.ClientConnector;
import com.gifisan.nio.client.ClientSesssion;
import com.gifisan.nio.jms.Message;
import com.gifisan.nio.jms.client.MessageBrowser;
import com.gifisan.nio.jms.client.impl.MessageBrowserImpl;

public class TestBrowser {

	public static void main(String[] args) throws Exception {

		ClientConnector connector = ClientUtil.getClientConnector();

		connector.connect();

		ClientSesssion session = connector.getClientSession();

		MessageBrowser browser = new MessageBrowserImpl(session);

		browser.login("admin", "admin100");

		Message message = browser.browser("wwww");

		System.out.println(message);

		browser.logout();

		connector.close();

	}
}
