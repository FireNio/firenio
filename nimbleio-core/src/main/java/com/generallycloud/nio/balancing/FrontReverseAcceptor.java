package com.generallycloud.nio.balancing;

import java.io.IOException;

import com.generallycloud.nio.acceptor.TCPAcceptor;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.LoggerUtil;
import com.generallycloud.nio.component.NIOContext;

public class FrontReverseAcceptor {

	private TCPAcceptor		acceptor	= new TCPAcceptor();

	protected void start(NIOContext context) throws IOException {

		this.acceptor.setContext(context);

		this.acceptor.bind();

		LoggerUtil.prettyNIOServerLog(LoggerFactory.getLogger(FrontReverseAcceptor.class),
				"Front Reverse Acceptor 启动成功 ...");
	}
	
	protected TCPAcceptor getAcceptor() {
		return acceptor;
	}

	protected void stop() {
		acceptor.unbind();
	}
}
