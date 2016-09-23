package com.generallycloud.test.nio.jms;

import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.extend.FixedSession;
import com.generallycloud.nio.extend.IOConnectorUtil;
import com.generallycloud.nio.extend.SimpleIOEventHandle;
import com.generallycloud.nio.extend.plugin.jms.Message;
import com.generallycloud.nio.extend.plugin.jms.client.MessageBrowser;
import com.generallycloud.nio.extend.plugin.jms.client.impl.DefaultMessageBrowser;

public class TestBrowser {

	public static void main(String[] args) throws Exception {
		
		String queueName = "qName";

		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		SocketChannelConnector connector = IOConnectorUtil.getTCPConnector(eventHandle);

		FixedSession session = eventHandle.getFixedSession();

		connector.connect();

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
