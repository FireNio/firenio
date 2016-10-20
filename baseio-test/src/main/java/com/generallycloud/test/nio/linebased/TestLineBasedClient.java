package com.generallycloud.test.nio.linebased;

import com.generallycloud.nio.codec.fixedlength.future.FixedLengthReadFutureImpl;
import com.generallycloud.nio.codec.line.LineBasedProtocolFactory;
import com.generallycloud.nio.codec.line.future.LineBasedReadFuture;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.BaseContextImpl;
import com.generallycloud.nio.component.IOEventHandleAdaptor;
import com.generallycloud.nio.component.LoggerSEListener;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.extend.ConnectorCloseSEListener;
import com.generallycloud.nio.protocol.ReadFuture;

public class TestLineBasedClient {

	public static void main(String[] args) throws Exception {

		IOEventHandleAdaptor eventHandleAdaptor = new IOEventHandleAdaptor() {

			public void accept(Session session, ReadFuture future) throws Exception {

				LineBasedReadFuture f = (LineBasedReadFuture) future;
				System.out.println();
				System.out.println("____________________"+f.getText());
				System.out.println();
			}
		};

		SocketChannelConnector connector = new SocketChannelConnector();
		
		ServerConfiguration configuration = new ServerConfiguration();
		
		configuration.setSERVER_HOST("localhost");
		configuration.setSERVER_TCP_PORT(18300);
		
		BaseContext context = new BaseContextImpl(configuration);

		context.setIOEventHandleAdaptor(eventHandleAdaptor);
		
		context.addSessionEventListener(new LoggerSEListener());

		context.addSessionEventListener(new ConnectorCloseSEListener(connector));

		context.setProtocolFactory(new LineBasedProtocolFactory());
		
		connector.setContext(context);
		
		Session session = connector.connect();

		ReadFuture future = new FixedLengthReadFutureImpl(context);

		future.write("hello server !");

		session.flush(future);
		
		ThreadUtil.sleep(100);

		CloseUtil.close(connector);

	}
}
