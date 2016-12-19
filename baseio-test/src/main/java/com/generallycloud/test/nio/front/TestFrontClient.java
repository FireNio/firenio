package com.generallycloud.test.nio.front;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import com.generallycloud.nio.codec.protobase.ProtobaseProtocolFactory;
import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFuture;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.DateUtil;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.IoEventHandleAdaptor;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.protocol.ReadFuture;
import com.generallycloud.test.nio.common.IoConnectorUtil;
import com.generallycloud.test.nio.common.ReadFutureFactory;

public class TestFrontClient {

	public static void main(String[] args) throws Exception {
		
		SharedBundle.instance().loadAllProperties("nio");
		
		final AtomicInteger res = new AtomicInteger();

		IoEventHandleAdaptor eventHandleAdaptor = new IoEventHandleAdaptor() {

			@Override
			public void accept(SocketSession session, ReadFuture future) throws Exception {
				
				ProtobaseReadFuture f = (ProtobaseReadFuture)future;
				
				System.out.println(f.getReadText()+"______"+DateUtil.now());
				
				res.incrementAndGet();
			}
		};

		ServerConfiguration configuration = new ServerConfiguration();

		configuration.setSERVER_PORT(8900);

		SocketChannelConnector connector = IoConnectorUtil.getTCPConnector(eventHandleAdaptor, configuration);

		connector.getContext().setProtocolFactory(new ProtobaseProtocolFactory());
		
		connector.connect();

		SocketSession session = connector.getSession();
		
		for (int i = 0; i < 10; i++) {

			int fid = Math.abs(new Random().nextInt());
			
			ProtobaseReadFuture future = ReadFutureFactory.create(session,fid, "service-name");

			future.write("你好！");
			
			future.setHashCode(fid);

			session.flush(future);
		}
		
		ThreadUtil.sleep(500);

		CloseUtil.close(connector);
		
		System.out.println("=========="+res.get());
	}

}
