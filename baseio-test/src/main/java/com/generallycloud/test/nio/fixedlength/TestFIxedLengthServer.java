package com.generallycloud.test.nio.fixedlength;

import com.generallycloud.nio.acceptor.SocketChannelAcceptor;
import com.generallycloud.nio.codec.fixedlength.FixedLengthProtocolFactory;
import com.generallycloud.nio.codec.fixedlength.future.FLBeatFutureFactory;
import com.generallycloud.nio.codec.fixedlength.future.FixedLengthReadFuture;
import com.generallycloud.nio.component.BaseContextImpl;
import com.generallycloud.nio.component.IOEventHandleAdaptor;
import com.generallycloud.nio.component.LoggerSEListener;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.SessionAliveSEListener;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.protocol.ReadFuture;

public class TestFIxedLengthServer {

	public static void main(String[] args) throws Exception {

		IOEventHandleAdaptor eventHandleAdaptor = new IOEventHandleAdaptor() {

			public void accept(Session session, ReadFuture future) throws Exception {
				FixedLengthReadFuture f = (FixedLengthReadFuture) future;
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
		
		context.addSessionEventListener(new SessionAliveSEListener());

		context.setIOEventHandleAdaptor(eventHandleAdaptor);
		
		context.setBeatFutureFactory(new FLBeatFutureFactory());

		context.setProtocolFactory(new FixedLengthProtocolFactory());

		acceptor.setContext(context);

		acceptor.bind();
	}
}
