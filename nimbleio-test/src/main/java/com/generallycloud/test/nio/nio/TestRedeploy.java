package com.generallycloud.test.nio.nio;

import com.generallycloud.nio.codec.nio.future.NIOReadFuture;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.extend.FixedSession;
import com.generallycloud.nio.extend.IOConnectorUtil;
import com.generallycloud.nio.extend.SimpleIOEventHandle;
import com.generallycloud.nio.extend.implementation.SYSTEMRedeployServlet;

public class TestRedeploy {

	public static void main(String[] args) throws Exception {

		String serviceKey = SYSTEMRedeployServlet.SERVICE_NAME;

		String param = "{username:\"admin\",password:\"admin100\"}";

		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		SocketChannelConnector connector = IOConnectorUtil.getTCPConnector(eventHandle);

		FixedSession session = eventHandle.getFixedSession();

		connector.connect();

		session.login("admin", "admin100");

		NIOReadFuture future = session.request(serviceKey, param);
		System.out.println(future.getText());
		
		for (int i = 0; i < 0; i++) {
			
			future = session.request(serviceKey, param);
			
			
		}
		

		CloseUtil.close(connector);
	}
}
