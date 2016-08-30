package com.generallycloud.nio.balancing;

import java.io.IOException;

import com.generallycloud.nio.acceptor.TCPAcceptor;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.LoggerUtil;
import com.generallycloud.nio.component.DefaultNIOContext;
import com.generallycloud.nio.component.NIOContext;
import com.generallycloud.nio.component.ServerConfiguration;

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
