package com.generallycloud.test.nio.linebased;

import com.generallycloud.nio.acceptor.SocketChannelAcceptor;
import com.generallycloud.nio.codec.line.LineBasedProtocolFactory;
import com.generallycloud.nio.codec.line.future.LineBasedReadFuture;
import com.generallycloud.nio.component.BaseContextImpl;
import com.generallycloud.nio.component.IOEventHandleAdaptor;
import com.generallycloud.nio.component.LoggerSEListener;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.protocol.ReadFuture;

public class TestLineBasedServer {

	public static void main(String[] args) throws Exception {

		IOEventHandleAdaptor eventHandleAdaptor = new IOEventHandleAdaptor() {

			public void accept(Session session, ReadFuture future) throws Exception {
				
				LineBasedReadFuture f = (LineBasedReadFuture) future;
				String res = "yes server already accept your message:" + f.getText();
				future.write(res);
				session.flush(future);
			}
		};

		ServerConfiguration configuration = new ServerConfiguration();
		
		configuration.setSERVER_TCP_PORT(18300);

		SocketChannelAcceptor acceptor = new SocketChannelAcceptor();

		BaseContext context = new BaseContextImpl(configuration);
		
		context.addSessionEventListener(new LoggerSEListener());
		
		context.setIOEventHandleAdaptor(eventHandleAdaptor);
		
		context.setProtocolFactory(new LineBasedProtocolFactory());

		acceptor.setContext(context);

		acceptor.bind();
	}
}
