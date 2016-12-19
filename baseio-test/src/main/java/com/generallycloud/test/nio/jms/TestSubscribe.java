package com.generallycloud.test.nio.jms;

import java.io.IOException;

import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.container.FixedSession;
import com.generallycloud.nio.container.SimpleIOEventHandle;
import com.generallycloud.nio.container.jms.MQException;
import com.generallycloud.nio.container.jms.Message;
import com.generallycloud.nio.container.jms.client.MessageConsumer;
import com.generallycloud.nio.container.jms.client.OnMessage;
import com.generallycloud.nio.container.jms.client.impl.DefaultMessageConsumer;
import com.generallycloud.test.nio.common.IoConnectorUtil;

public class TestSubscribe {

	public static void main(String[] args) throws IOException, MQException {

		for (int i = 0; i < 5; i++) {

			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						TestSubscribe.test();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();

		}

	}

	private static void test() throws Exception {

		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		SocketChannelConnector connector = IoConnectorUtil.getTCPConnector(eventHandle);

		FixedSession session = new FixedSession(connector.connect());

		session.login("admin", "admin100");

		MessageConsumer consumer = new DefaultMessageConsumer(session);

		consumer.subscribe(new OnMessage() {

			@Override
			public void onReceive(Message message) {
				System.out.println(message);
			}
		});

		connector.close();
	}

}
