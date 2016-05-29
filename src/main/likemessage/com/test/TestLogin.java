package com.test;

import java.io.IOException;

import com.gifisan.nio.Encoding;
import com.gifisan.nio.client.ClientTCPConnector;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.MD5Token;
import com.gifisan.nio.common.ThreadUtil;

public class TestLogin {

	public static void main(String[] args) throws IOException {

		final ClientTCPConnector connector = ClientUtil.getClientConnector();

		connector.connect();

		String username = "wk";
		String password = "wk";
		
		boolean b = connector.login(username, password);

		System.out.println(MD5Token.getInstance().getLongToken("admin100", Encoding.DEFAULT));

		System.out.println(b);

		ThreadUtil.sleep(500);

		CloseUtil.close(connector);

	}
}
