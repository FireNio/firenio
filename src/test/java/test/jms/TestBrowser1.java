package test.jms;

import test.ClientUtil;

import com.gifisan.mtp.client.ClientConnector;
import com.gifisan.mtp.client.ClientSesssion;
import com.gifisan.mtp.jms.Message;
import com.gifisan.mtp.jms.client.MessageBrowser;
import com.gifisan.mtp.jms.client.impl.MessageBrowserImpl;

public class TestBrowser1 {

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
