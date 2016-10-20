package com.generallycloud.test.nio.base;

import com.generallycloud.nio.codec.base.future.BaseReadFuture;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.component.OnReadFuture;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.extend.FixedSession;
import com.generallycloud.nio.extend.IOConnectorUtil;
import com.generallycloud.nio.extend.SimpleIOEventHandle;
import com.generallycloud.nio.protocol.ReadFuture;

public class TestSessionDisconnect {

	public static void main(String[] args) throws Exception {
		
		SharedBundle.instance().loadAllProperties("nio");

		String serviceName = "TestSessionDisconnectServlet";
		
		String param = "ttt";

		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		SocketChannelConnector connector = IOConnectorUtil.getTCPConnector(eventHandle);

		FixedSession session = eventHandle.getFixedSession();

		connector.connect();

		session.login("admin", "admin100");

		BaseReadFuture future = session.request(serviceName, param);
		System.out.println(future.getText());

		session.listen(serviceName, new OnReadFuture() {
			public void onResponse(Session session, ReadFuture future) {
				
				BaseReadFuture f = (BaseReadFuture) future;
				System.out.println(f.getText());
			}
		});

		session.write(serviceName, param);

	}
}
