package com.generallycloud.test.nio.load;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.IOEventHandleAdaptor;
import com.generallycloud.nio.component.ReadFutureFactory;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.protocol.ReadFuture;
import com.generallycloud.nio.component.protocol.nio.NIOProtocolFactory;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.extend.IOConnectorUtil;

public class TestSimpleClient {

	public static void main(String[] args) throws Exception {

		SharedBundle.instance().loadAllProperties("nio");

		IOEventHandleAdaptor eventHandleAdaptor = new IOEventHandleAdaptor() {

			public void accept(Session session, ReadFuture future) throws Exception {
				System.out.println(future);
			}
		};

		SocketChannelConnector connector = IOConnectorUtil.getTCPConnector(eventHandleAdaptor);

		connector.getContext().setProtocolFactory(new NIOProtocolFactory());
		
		connector.connect();

		Session session = connector.getSession();

		ReadFuture future = ReadFutureFactory.create(session, "test", session.getContext().getIOEventHandleAdaptor());

		future.write("hello server !");

		session.flush(future);
		
		ThreadUtil.sleep(500);

		CloseUtil.close(connector);

	}
}
