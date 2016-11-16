package com.generallycloud.test.nio.base;

import com.generallycloud.nio.codec.base.future.BaseReadFuture;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.extend.FixedSession;
import com.generallycloud.nio.extend.SimpleIOEventHandle;
import com.generallycloud.nio.extend.example.baseio.TestGetPhoneNOServlet;
import com.generallycloud.test.nio.common.IOConnectorUtil;

public class TestGetPhoneNO {
	
	
	public static void main(String[] args) throws Exception {
		
		SharedBundle.instance().loadAllProperties("nio");

		String serviceKey = TestGetPhoneNOServlet.SERVICE_NAME;
		
		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		SocketChannelConnector connector = IOConnectorUtil.getTCPConnector(eventHandle);

		FixedSession session = new FixedSession(connector.connect());

		session.login("admin", "admin100");
		
		BaseReadFuture future = session.request(serviceKey, null);
		System.out.println(future.getReadText());
		
		CloseUtil.close(connector);
		
	}
}
