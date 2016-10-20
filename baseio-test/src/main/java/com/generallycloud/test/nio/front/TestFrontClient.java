package com.generallycloud.test.nio.front;

import java.util.Random;

import com.generallycloud.nio.balance.FrontContext;
import com.generallycloud.nio.codec.nio.NIOProtocolFactory;
import com.generallycloud.nio.codec.nio.future.NIOReadFuture;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.IOEventHandleAdaptor;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.extend.IOConnectorUtil;
import com.generallycloud.nio.protocol.ReadFuture;
import com.generallycloud.test.nio.common.ReadFutureFactory;

public class TestFrontClient {

	public static void main(String[] args) throws Exception {
		
		SharedBundle.instance().loadAllProperties("nio");

		IOEventHandleAdaptor eventHandleAdaptor = new IOEventHandleAdaptor() {

			public void accept(Session session, ReadFuture future) throws Exception {
				
				NIOReadFuture f = (NIOReadFuture)future;
				
				System.out.println(f.getText());
			}
		};

		ServerConfiguration configuration = new ServerConfiguration();

		configuration.setSERVER_TCP_PORT(8600);

		SocketChannelConnector connector = IOConnectorUtil.getTCPConnector(eventHandleAdaptor, configuration);

		connector.getContext().setProtocolFactory(new NIOProtocolFactory());
		
		connector.connect();

		Session session = connector.getSession();
		
		NIOReadFuture future = ReadFutureFactory.create(session,new Random().nextInt(), FrontContext.FRONT_RECEIVE_BROADCAST);

		future.write("你好！");

		session.flush(future);

		for (int i = 0; i < 100; i++) {

			int fid = Math.abs(new Random().nextInt());
			
			future = ReadFutureFactory.create(session,fid, "service-name");

			future.write("你好！");
			
			future.setHashCode(fid);

			session.flush(future);
		}

		ThreadUtil.sleep(100);

		CloseUtil.close(connector);
	}

}
