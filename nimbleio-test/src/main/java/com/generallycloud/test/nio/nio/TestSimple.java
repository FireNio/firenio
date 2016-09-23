package com.generallycloud.test.nio.nio;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.OnReadFuture;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.protocol.ReadFuture;
import com.generallycloud.nio.component.protocol.nio.NIOProtocolFactory;
import com.generallycloud.nio.component.protocol.nio.future.NIOReadFuture;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.extend.FixedSession;
import com.generallycloud.nio.extend.IOConnectorUtil;
import com.generallycloud.nio.extend.SimpleIOEventHandle;
import com.generallycloud.nio.extend.implementation.SYSTEMShowMemoryServlet;

public class TestSimple {
	
	
	public static void main(String[] args) throws Exception {

		SharedBundle.instance().loadAllProperties("nio");

		String serviceKey = "TestSimpleServlet";
		
		String param = "ttt";
		
		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		SocketChannelConnector connector = IOConnectorUtil.getTCPConnector(eventHandle);
		
		connector.getContext().setProtocolFactory(new NIOProtocolFactory());

		FixedSession session = eventHandle.getFixedSession();
		connector.connect();
		session.login("admin", "admin100");
		
		NIOReadFuture future = session.request(serviceKey, param);
		System.out.println(future.getText());
		
		session.listen(serviceKey, new OnReadFuture() {
			
			public void onResponse(Session session, ReadFuture future) {
				
				NIOReadFuture f = (NIOReadFuture) future;
				System.out.println(f.getText());
			}
		});
		
		session.write(serviceKey, param);
		
		future = session.request(SYSTEMShowMemoryServlet.SERVICE_NAME, param);
		System.out.println(future.getText());
		System.out.println("__________"+session.getSession().getSessionID());
		
//		response = session.request(serviceKey, param);
//		System.out.println(response.getContent());
		
		ThreadUtil.sleep(500);
		
		CloseUtil.close(connector);
	}
}
