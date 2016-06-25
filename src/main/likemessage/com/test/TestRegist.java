package com.test;

import java.io.IOException;

import com.gifisan.nio.client.FixedSession;
import com.gifisan.nio.client.TCPConnector;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.component.ClientLauncher;
import com.gifisan.nio.server.RESMessage;
import com.likemessage.client.LMClient;

public class TestRegist {

	public static void main(String[] args) throws IOException {

		ClientLauncher launcher = new ClientLauncher();
		
		TCPConnector connector = launcher.getTCPConnector();

		connector.connect();
		
		FixedSession session = launcher.getFixedSession();

		LMClient client = new LMClient();

		RESMessage message = client.regist(session, "zhangfei", "zhangfei","张飞");

		System.out.println(message);

		ThreadUtil.sleep(500);

		CloseUtil.close(connector);

	}
}
