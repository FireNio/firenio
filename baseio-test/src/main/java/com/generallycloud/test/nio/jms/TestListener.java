package com.generallycloud.test.nio.jms;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.extend.FixedSession;
import com.generallycloud.nio.extend.IOConnectorUtil;
import com.generallycloud.nio.extend.SimpleIOEventHandle;
import com.generallycloud.nio.extend.plugin.jms.Message;
import com.generallycloud.nio.extend.plugin.jms.client.MessageConsumer;
import com.generallycloud.nio.extend.plugin.jms.client.OnMessage;
import com.generallycloud.nio.extend.plugin.jms.client.impl.DefaultMessageConsumer;

public class TestListener {

	public static void main(String[] args) throws Exception {
		
		SharedBundle.instance().loadAllProperties("nio");

		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		SocketChannelConnector connector = IOConnectorUtil.getTCPConnector(eventHandle);

		FixedSession session = eventHandle.getFixedSession();

		connector.connect();

		session.login("wk", "wk");

		MessageConsumer consumer = new DefaultMessageConsumer(session);

		long old = System.currentTimeMillis();

		consumer.receive(new OnMessage() {

			public void onReceive(Message message) {
				System.out.println(message);
			}
		});

		System.out.println("Time:" + (System.currentTimeMillis() - old));

		ThreadUtil.sleep(1500000);
		
		CloseUtil.close(connector);

	}

}
