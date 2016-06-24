package com.test;

import java.io.IOException;

import com.gifisan.nio.client.ConnectorSession;
import com.gifisan.nio.client.TCPConnector;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.server.RESMessage;
import com.likemessage.client.LMClient;

public class TestRegist {

	public static void main(String[] args) throws IOException {

		final TCPConnector connector = ClientUtil.getClientConnector();

		connector.connect();

		ConnectorSession session = connector.getClientSession();

		LMClient client = new LMClient();

		RESMessage message = client.regist(session, "zhangfei", "zhangfei","张飞");

		System.out.println(message);

		ThreadUtil.sleep(500);

		CloseUtil.close(connector);

	}
}
