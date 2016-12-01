package com.generallycloud.test.nio.jms;

import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.container.FixedSession;
import com.generallycloud.nio.container.SimpleIOEventHandle;
import com.generallycloud.nio.container.jms.Message;
import com.generallycloud.nio.container.jms.client.MessageBrowser;
import com.generallycloud.nio.container.jms.client.impl.DefaultMessageBrowser;
import com.generallycloud.test.nio.common.IoConnectorUtil;

public class TestBrowser {

	public static void main(String[] args) throws Exception {
		
		String queueName = "qName";

		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		SocketChannelConnector connector = IoConnectorUtil.getTCPConnector(eventHandle);

		FixedSession session = new FixedSession(connector.connect());

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
