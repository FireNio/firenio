package com.generallycloud.test.nio.base;

import com.generallycloud.nio.codec.base.future.BaseReadFuture;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.OnReadFuture;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.extend.FixedSession;
import com.generallycloud.nio.extend.IOConnectorUtil;
import com.generallycloud.nio.extend.SimpleIOEventHandle;
import com.generallycloud.nio.protocol.ReadFuture;

public class TestListenSimple {
	
	
	public static void main(String[] args) throws Exception {

		SharedBundle.instance().loadAllProperties("nio");
		String serviceKey = "TestListenSimpleServlet";
		String param = "ttt";
		
		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		SocketChannelConnector connector = IOConnectorUtil.getTCPConnector(eventHandle);

		FixedSession session = eventHandle.getFixedSession();

		connector.connect();

		BaseReadFuture future = session.request(serviceKey, param);
		System.out.println(future.getText());
		
		session.listen(serviceKey,new OnReadFuture() {
			
			public void onResponse(Session session, ReadFuture future) {
				BaseReadFuture f = (BaseReadFuture) future;
				System.out.println(f.getText());
			}
		});
		
		session.write(serviceKey, param);
		
		ThreadUtil.sleep(1000);
		CloseUtil.close(connector);
		
	}
}
