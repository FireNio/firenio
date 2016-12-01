package com.generallycloud.test.nio.base;

import com.generallycloud.nio.codec.base.BaseProtocolFactory;
import com.generallycloud.nio.codec.base.future.BaseReadFuture;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.OnReadFuture;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.container.FixedSession;
import com.generallycloud.nio.container.SimpleIOEventHandle;
import com.generallycloud.nio.container.implementation.SYSTEMShowMemoryServlet;
import com.generallycloud.nio.protocol.ReadFuture;
import com.generallycloud.test.nio.common.IoConnectorUtil;

public class TestSimple {
	
	
	public static void main(String[] args) throws Exception {

		SharedBundle.instance().loadAllProperties("nio");

		String serviceKey = "TestSimpleServlet";
		
		String param = "ttt";
		
		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		SocketChannelConnector connector = IoConnectorUtil.getTCPConnector(eventHandle);
		
		connector.getContext().setProtocolFactory(new BaseProtocolFactory());

		FixedSession session = new FixedSession(connector.connect());
		
		session.login("admin", "admin100");
		
		BaseReadFuture future = session.request(serviceKey, param);
		
		System.out.println(future.getReadText());
		
		session.listen(serviceKey, new OnReadFuture() {
			
			public void onResponse(SocketSession session, ReadFuture future) {
				
				BaseReadFuture f = (BaseReadFuture) future;
				System.out.println(f.getReadText());
			}
		});
		
		session.write(serviceKey, param);
		
		future = session.request(SYSTEMShowMemoryServlet.SERVICE_NAME, param);
		System.out.println(future.getReadText());
		System.out.println("__________"+session.getSession().getSessionID());
		
//		response = session.request(serviceKey, param);
//		System.out.println(response.getContent());
		
		ThreadUtil.sleep(500);
		
		CloseUtil.close(connector);
	}
}
