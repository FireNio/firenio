package com.test;

import java.io.IOException;

import com.gifisan.nio.Encoding;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.MD5Token;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.extend.FixedSession;
import com.gifisan.nio.extend.IOConnectorUtil;
import com.gifisan.nio.extend.SimpleIOEventHandle;

public class TestLogin {

	public static void main(String[] args) throws IOException {

		String username = "wk";
		String password = "wk";

		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		TCPConnector connector = IOConnectorUtil.getTCPConnector(eventHandle);

		FixedSession session = eventHandle.getFixedSession();

		connector.connect();
		
		boolean b = session.login(username, password);

		System.out.println(MD5Token.getInstance().getLongToken("admin100", Encoding.DEFAULT));

		System.out.println(b);

		ThreadUtil.sleep(500);

		CloseUtil.close(connector);

	}
}
