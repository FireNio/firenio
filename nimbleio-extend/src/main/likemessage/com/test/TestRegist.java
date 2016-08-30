package com.test;

import java.io.IOException;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.connector.TCPConnector;
import com.generallycloud.nio.extend.FixedSession;
import com.generallycloud.nio.extend.IOConnectorUtil;
import com.generallycloud.nio.extend.RESMessage;
import com.generallycloud.nio.extend.SimpleIOEventHandle;
import com.likemessage.client.LMClient;

public class TestRegist {

	public static void main(String[] args) throws IOException {

		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		TCPConnector connector = IOConnectorUtil.getTCPConnector(eventHandle);

		FixedSession session = eventHandle.getFixedSession();

		connector.connect();

		LMClient client = new LMClient();

		RESMessage message = client.regist(session, "zhangfei", "zhangfei","张飞");

		System.out.println(message);

		ThreadUtil.sleep(500);

		CloseUtil.close(connector);

	}
}
