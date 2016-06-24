package com.test;

import java.io.IOException;

import com.gifisan.nio.client.ConnectorSession;
import com.gifisan.nio.client.TCPConnector;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.server.RESMessage;
import com.likemessage.bean.T_MESSAGE;
import com.likemessage.client.LMClient;

public class TestAddMessage {

	public static void main(String[] args) throws IOException {

		final TCPConnector connector = ClientUtil.getClientConnector();

		connector.connect();

		connector.login("wk", "wk");

		ConnectorSession session = connector.getClientSession();

		LMClient client = new LMClient();

		T_MESSAGE m = new T_MESSAGE();

		m.setDeleted(false);
		m.setFromUserID(2);
		m.setMessage("wwww1111");
		m.setMsgDate(System.currentTimeMillis());
		m.setMsgType(0);
		m.setSend(true);
		m.setToUserID(1);

		RESMessage message = client.addMessage(session, m, session.getAuthority().getUuid());

		System.out.println(message);

		ThreadUtil.sleep(500);

		CloseUtil.close(connector);

	}
}
