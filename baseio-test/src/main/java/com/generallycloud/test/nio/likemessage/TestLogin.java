package com.generallycloud.test.nio.likemessage;

import com.generallycloud.nio.Encoding;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.MD5Token;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.extend.FixedSession;
import com.generallycloud.nio.extend.IOConnectorUtil;
import com.generallycloud.nio.extend.SimpleIOEventHandle;

public class TestLogin {

	public static void main(String[] args) throws Exception {

		String username = "wk";
		String password = "wk";

		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		SocketChannelConnector connector = IOConnectorUtil.getTCPConnector(eventHandle);

		FixedSession session = eventHandle.getFixedSession();

		connector.connect();
		
		boolean b = session.login(username, password);

		System.out.println(MD5Token.getInstance().getLongToken("admin100", Encoding.UTF8));

		System.out.println(b);

		ThreadUtil.sleep(500);

		CloseUtil.close(connector);

	}
}
