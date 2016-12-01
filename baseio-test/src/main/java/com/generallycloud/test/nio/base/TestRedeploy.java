package com.generallycloud.test.nio.base;

import com.generallycloud.nio.codec.base.future.BaseReadFuture;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.container.FixedSession;
import com.generallycloud.nio.container.SimpleIOEventHandle;
import com.generallycloud.nio.container.implementation.SYSTEMRedeployServlet;
import com.generallycloud.test.nio.common.IoConnectorUtil;

public class TestRedeploy {

	public static void main(String[] args) throws Exception {

		String serviceKey = SYSTEMRedeployServlet.SERVICE_NAME;

		String param = "{username:\"admin\",password:\"admin100\"}";

		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		SocketChannelConnector connector = IoConnectorUtil.getTCPConnector(eventHandle);

		FixedSession session = new FixedSession(connector.connect());

		session.login("admin", "admin100");

		BaseReadFuture future = session.request(serviceKey, param);
		System.out.println(future.getReadText());
		
		for (int i = 0; i < 0; i++) {
			
			future = session.request(serviceKey, param);
			
			
		}
		

		CloseUtil.close(connector);
	}
}
