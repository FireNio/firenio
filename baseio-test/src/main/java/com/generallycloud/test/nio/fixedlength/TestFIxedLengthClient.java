package com.generallycloud.test.nio.fixedlength;

import com.generallycloud.nio.codec.fixedlength.FixedLengthProtocolFactory;
import com.generallycloud.nio.codec.fixedlength.future.FLBeatFutureFactory;
import com.generallycloud.nio.codec.fixedlength.future.FixedLengthReadFuture;
import com.generallycloud.nio.codec.fixedlength.future.FixedLengthReadFutureImpl;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.common.ssl.SSLUtil;
import com.generallycloud.nio.common.ssl.SslContext;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.BaseContextImpl;
import com.generallycloud.nio.component.IOEventHandleAdaptor;
import com.generallycloud.nio.component.LoggerSEListener;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.protocol.ReadFuture;

public class TestFIxedLengthClient {

	public static void main(String[] args) throws Exception {

		IOEventHandleAdaptor eventHandleAdaptor = new IOEventHandleAdaptor() {

			public void accept(Session session, ReadFuture future) throws Exception {

				FixedLengthReadFuture f = (FixedLengthReadFuture) future;
				System.out.println();
				System.out.println("____________________"+f.getReadText());
				System.out.println();
			}
		};
		
		SslContext sslContext = SSLUtil.initClient();

		SocketChannelConnector connector = new SocketChannelConnector();
		
		ServerConfiguration configuration = new ServerConfiguration("localhost", 18300);
		
		configuration.setSERVER_MEMORY_POOL_CAPACITY_RATE(0.1);
		
		BaseContext context = new BaseContextImpl(configuration);

		context.setIOEventHandleAdaptor(eventHandleAdaptor);
		
		context.addSessionEventListener(new LoggerSEListener());

//		context.addSessionEventListener(new SessionActiveSEListener());
		
		context.setBeatFutureFactory(new FLBeatFutureFactory());

		context.setProtocolFactory(new FixedLengthProtocolFactory());
		
		context.setSslContext(sslContext);
		
		connector.setContext(context);
		
		Session session = connector.connect();

		FixedLengthReadFuture future = new FixedLengthReadFutureImpl(context);

		future.write("hello server !");

		session.flush(future);
		
		ThreadUtil.sleep(100);

		CloseUtil.close(connector);

	}
}
