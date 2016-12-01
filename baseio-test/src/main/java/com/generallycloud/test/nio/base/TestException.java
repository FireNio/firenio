package com.generallycloud.test.nio.base;

import com.generallycloud.nio.codec.base.future.BaseReadFuture;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.container.FixedSession;
import com.generallycloud.nio.container.SimpleIOEventHandle;
import com.generallycloud.test.nio.common.IoConnectorUtil;

public class TestException {
	
	
	public static void main(String[] args) throws Exception {

		String serviceKey = "TestExceptionServlet";
		String param = "ttt";
		
		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		SocketChannelConnector connector = IoConnectorUtil.getTCPConnector(eventHandle);

		FixedSession session = new FixedSession(connector.connect());

		BaseReadFuture future = session.request(serviceKey, param);
		System.out.println(future.getReadText());
		
		CloseUtil.close(connector);
	}
}
