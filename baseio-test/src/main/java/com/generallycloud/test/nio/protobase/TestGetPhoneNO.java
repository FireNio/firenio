package com.generallycloud.test.nio.protobase;

import com.generallycloud.nio.codec.protobase.ProtobaseProtocolFactory;
import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFuture;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.container.FixedSession;
import com.generallycloud.nio.container.SimpleIOEventHandle;
import com.generallycloud.nio.container.protobase.example.TestGetPhoneNOServlet;
import com.generallycloud.test.nio.common.IoConnectorUtil;

public class TestGetPhoneNO {
	
	
	public static void main(String[] args) throws Exception {
		
		SharedBundle.instance().loadAllProperties("nio");

		String serviceKey = TestGetPhoneNOServlet.SERVICE_NAME;
		
		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		SocketChannelConnector connector = IoConnectorUtil.getTCPConnector(eventHandle);
		
		connector.getContext().setProtocolFactory(new ProtobaseProtocolFactory());

		FixedSession session = new FixedSession(connector.connect());

		session.login("admin", "admin100");
		
		ProtobaseReadFuture future = session.request(serviceKey, null);
		
		System.out.println(future.getReadText());
		
		CloseUtil.close(connector);
		
	}
}
