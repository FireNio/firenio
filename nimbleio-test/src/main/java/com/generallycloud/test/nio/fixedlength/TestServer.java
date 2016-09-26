package com.generallycloud.test.nio.fixedlength;

import com.generallycloud.nio.acceptor.SocketChannelAcceptor;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.component.DefaultNIOContext;
import com.generallycloud.nio.component.IOEventHandleAdaptor;
import com.generallycloud.nio.component.LoggerSEListener;
import com.generallycloud.nio.component.NIOContext;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.SessionAliveSEListener;
import com.generallycloud.nio.component.concurrent.EventLoopGroup;
import com.generallycloud.nio.component.concurrent.SingleEventLoopGroup;
import com.generallycloud.nio.component.protocol.ReadFuture;
import com.generallycloud.nio.component.protocol.fixedlength.FixedLengthProtocolFactory;
import com.generallycloud.nio.component.protocol.fixedlength.future.FLBeatFutureFactory;
import com.generallycloud.nio.component.protocol.fixedlength.future.FixedLengthReadFuture;
import com.generallycloud.nio.configuration.PropertiesSCLoader;
import com.generallycloud.nio.configuration.ServerConfiguration;

public class TestServer {

	public static void main(String[] args) throws Exception {

		SharedBundle.instance().loadAllProperties("nio");
		
		IOEventHandleAdaptor eventHandleAdaptor = new IOEventHandleAdaptor() {

			public void accept(Session session, ReadFuture future) throws Exception {
				FixedLengthReadFuture f = (FixedLengthReadFuture) future;
				String res = "yes server already accept your message:" + f.getText();
				future.write(res);
				session.flush(future);
			}
		};

		PropertiesSCLoader loader = new PropertiesSCLoader();
		
		ServerConfiguration configuration = loader.loadConfiguration(SharedBundle.instance());

		SocketChannelAcceptor acceptor = new SocketChannelAcceptor();

		EventLoopGroup eventLoopGroup = new SingleEventLoopGroup(
				"IOEvent",
				configuration.getSERVER_CHANNEL_QUEUE_SIZE(),
				configuration.getSERVER_CORE_SIZE());

		NIOContext context = new DefaultNIOContext(configuration, eventLoopGroup);
		
		context.addSessionEventListener(new SessionAliveSEListener());

		context.setIOEventHandleAdaptor(eventHandleAdaptor);

		context.addSessionEventListener(new LoggerSEListener());
		
		context.setBeatFutureFactory(new FLBeatFutureFactory());

		acceptor.setContext(context);

		acceptor.getContext().setProtocolFactory(new FixedLengthProtocolFactory());

		acceptor.bind();
	}
}
