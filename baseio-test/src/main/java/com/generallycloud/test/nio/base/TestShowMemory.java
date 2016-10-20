package com.generallycloud.test.nio.base;

import com.generallycloud.nio.codec.base.BaseProtocolFactory;
import com.generallycloud.nio.codec.base.future.BaseReadFuture;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.extend.FixedSession;
import com.generallycloud.nio.extend.IOConnectorUtil;
import com.generallycloud.nio.extend.SimpleIOEventHandle;
import com.generallycloud.nio.extend.implementation.SYSTEMShowMemoryServlet;

public class TestShowMemory {

	
	public static void main(String[] args) throws Exception {
		
		SharedBundle.instance().loadAllProperties("nio");
		
		String serviceKey = SYSTEMShowMemoryServlet.SERVICE_NAME;
		
		String param = "{username:\"admin\",password:\"admin100\"}";
		
		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		SocketChannelConnector connector = IOConnectorUtil.getTCPConnector(eventHandle);
		
		connector.getContext().setProtocolFactory(new BaseProtocolFactory());

		FixedSession session = eventHandle.getFixedSession();

		connector.connect();

		session.login("admin", "admin100");
		
		BaseReadFuture future = session.request(serviceKey, param);
		System.out.println(future.getText());
		
		CloseUtil.close(connector);
		
	}
}
