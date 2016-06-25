package com.test;

import java.io.IOException;

import com.gifisan.nio.Encoding;
import com.gifisan.nio.client.FixedSession;
import com.gifisan.nio.client.TCPConnector;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.MD5Token;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.component.ClientLauncher;

public class TestLogin {

	public static void main(String[] args) throws IOException {

		String username = "wk";
		String password = "wk";

		ClientLauncher launcher = new ClientLauncher();
		
		TCPConnector connector = launcher.getTCPConnector();

		connector.connect();
		
		FixedSession session = launcher.getFixedSession();
		
		boolean b = session.login(username, password);

		System.out.println(MD5Token.getInstance().getLongToken("admin100", Encoding.DEFAULT));

		System.out.println(b);

		ThreadUtil.sleep(500);

		CloseUtil.close(connector);

	}
}
