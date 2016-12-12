package com.generallycloud.nio.balance;

import java.io.IOException;

import com.generallycloud.nio.acceptor.SocketChannelAcceptor;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.LoggerUtil;
import com.generallycloud.nio.component.SocketChannelContext;

public class BalanceReverseAcceptor {

	private SocketChannelAcceptor		acceptor	= null;

	protected void start(SocketChannelContext context) throws IOException {

		this.acceptor = new SocketChannelAcceptor(context);

		this.acceptor.bind();

		LoggerUtil.prettyNIOServerLog(LoggerFactory.getLogger(BalanceReverseAcceptor.class),
				"Balance Reverse Acceptor 启动成功 ...");
	}
	
	protected SocketChannelAcceptor getAcceptor() {
		return acceptor;
	}

	protected void stop() {
		CloseUtil.unbind(acceptor);
	}
}
