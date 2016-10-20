package com.generallycloud.test.nio.likemessage;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.extend.FixedSession;
import com.generallycloud.nio.extend.IOConnectorUtil;
import com.generallycloud.nio.extend.RESMessage;
import com.generallycloud.nio.extend.SimpleIOEventHandle;
import com.likemessage.client.LMClient;

public class TestRegist {

	public static void main(String[] args) throws Exception {

		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		SocketChannelConnector connector = IOConnectorUtil.getTCPConnector(eventHandle);

		FixedSession session = eventHandle.getFixedSession();

		connector.connect();

		LMClient client = new LMClient();

		RESMessage message = client.regist(session, "zhangfei", "zhangfei","张飞");

		System.out.println(message);

		ThreadUtil.sleep(500);

		CloseUtil.close(connector);

	}
}
