package com.generallycloud.test.nio.front;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import com.generallycloud.nio.codec.base.BaseProtocolFactory;
import com.generallycloud.nio.codec.base.future.BaseReadFuture;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.DateUtil;
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
		
		final AtomicInteger res = new AtomicInteger();

		IOEventHandleAdaptor eventHandleAdaptor = new IOEventHandleAdaptor() {

			public void accept(Session session, ReadFuture future) throws Exception {
				
				BaseReadFuture f = (BaseReadFuture)future;
				
				System.out.println(f.getText()+"______"+DateUtil.now());
				
				res.incrementAndGet();
			}
		};

		ServerConfiguration configuration = new ServerConfiguration();

		configuration.setSERVER_TCP_PORT(8600);

		SocketChannelConnector connector = IOConnectorUtil.getTCPConnector(eventHandleAdaptor, configuration);

		connector.getContext().setProtocolFactory(new BaseProtocolFactory());
		
		connector.connect();

		Session session = connector.getSession();
		
		for (int i = 0; i < 100; i++) {

			int fid = Math.abs(new Random().nextInt());
			
			BaseReadFuture future = ReadFutureFactory.create(session,fid, "service-name");

			future.write("你好！");
			
			future.setHashCode(fid);

			session.flush(future);
		}
		
		ThreadUtil.sleep(500);

		CloseUtil.close(connector);
		
		System.out.println("=========="+res.get());
	}

}
