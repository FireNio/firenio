package com.generallycloud.test.nio.linebased;

import com.generallycloud.nio.codec.linebased.LineBasedProtocolFactory;
import com.generallycloud.nio.codec.linebased.future.LineBasedReadFuture;
import com.generallycloud.nio.codec.linebased.future.LineBasedReadFutureImpl;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.IoEventHandleAdaptor;
import com.generallycloud.nio.component.LoggerSocketSEListener;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.SocketChannelContextImpl;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.protocol.ReadFuture;

public class TestLineBasedClient {

	public static void main(String[] args) throws Exception {

		IoEventHandleAdaptor eventHandleAdaptor = new IoEventHandleAdaptor() {

			@Override
			public void accept(SocketSession session, ReadFuture future) throws Exception {

				System.out.println();
				System.out.println("____________________"+future.getReadText());
				System.out.println();
			}
		};
		
		SocketChannelContext context = new SocketChannelContextImpl(new ServerConfiguration(18300));

		SocketChannelConnector connector = new SocketChannelConnector(context);

		context.setIoEventHandleAdaptor(eventHandleAdaptor);
		
		context.addSessionEventListener(new LoggerSocketSEListener());

		context.setProtocolFactory(new LineBasedProtocolFactory());
		
		SocketSession session = connector.connect();

		LineBasedReadFuture future = new LineBasedReadFutureImpl(context);

		future.write("hello server!");

		session.flush(future);
		
		ThreadUtil.sleep(100);

		CloseUtil.close(connector);

	}
}
