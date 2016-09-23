package com.generallycloud.test.nio.likemessage;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.extend.FixedSession;
import com.generallycloud.nio.extend.IOConnectorUtil;
import com.generallycloud.nio.extend.RESMessage;
import com.generallycloud.nio.extend.SimpleIOEventHandle;
import com.likemessage.bean.T_MESSAGE;
import com.likemessage.client.LMClient;

public class TestAddMessage {

	public static void main(String[] args) throws Exception {

		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		SocketChannelConnector connector = IOConnectorUtil.getTCPConnector(eventHandle);

		FixedSession session = eventHandle.getFixedSession();

		connector.connect();

		session.login("wk", "wk");

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
