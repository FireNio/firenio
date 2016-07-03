package com.gifisan.nio.front;

import java.io.IOException;

import com.gifisan.nio.acceptor.TCPAcceptor;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.LoggerUtil;
import com.gifisan.nio.component.DefaultNIOContext;
import com.gifisan.nio.component.NIOContext;
import com.gifisan.nio.extend.configuration.ServerConfiguration;

public class FrontReverseAcceptor {

	private TCPAcceptor	acceptor	= new TCPAcceptor();

	protected void start(FrontContext frontContext) throws IOException {

		FrontConfiguration configuration = frontContext.getFrontConfiguration();

		NIOContext context = new DefaultNIOContext();

		context.setIOEventHandleAdaptor(frontContext.getFrontReverseAcceptorHandler());

		context.addSessionEventListener(frontContext.getFrontReverseAcceptorSEListener());

		ServerConfiguration serverConfiguration = new ServerConfiguration();

		serverConfiguration.setSERVER_TCP_PORT(configuration.getFRONT_REVERSE_PORT());

		context.setServerConfiguration(serverConfiguration);

		acceptor.setContext(context);

		acceptor.bind();

		LoggerUtil.prettyNIOServerLog(LoggerFactory.getLogger(FrontReverseAcceptor.class),
				"Front Reverse Acceptor 启动成功 ...");
	}

	protected void stop() {
		acceptor.unbind();
	}
}
