package com.generallycloud.test.nio.base;

import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFuture;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.container.FixedSession;
import com.generallycloud.nio.container.SimpleIOEventHandle;
import com.generallycloud.nio.container.implementation.SYSTEMStopServerServlet;
import com.generallycloud.test.nio.common.IoConnectorUtil;

public class TestStopServer {

	public static void main(String[] args) throws Exception {
		String serviceKey = SYSTEMStopServerServlet.SERVICE_NAME;

		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		SocketChannelConnector connector = IoConnectorUtil.getTCPConnector(eventHandle);

		FixedSession session = new FixedSession(connector.connect());

		session.login("admin", "admin100");

		ProtobaseReadFuture future = session.request(serviceKey, null);
		System.out.println(future.getReadText());

		CloseUtil.close(connector);

	}
}
