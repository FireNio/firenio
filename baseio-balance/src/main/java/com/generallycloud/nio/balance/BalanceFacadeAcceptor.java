package com.generallycloud.nio.balance;

import java.io.IOException;

import com.generallycloud.nio.acceptor.SocketChannelAcceptor;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.LoggerUtil;
import com.generallycloud.nio.component.SocketChannelContext;

public class BalanceFacadeAcceptor {

	private byte[]			runLock			= new byte[] {};
	private boolean			running			= false;
	private BalanceContext		balanceContext		= null;
	private SocketChannelAcceptor	channelAcceptor	= null;

	public void start(BalanceContext balanceContext, SocketChannelContext facadeContext,
			SocketChannelContext reverseContext) throws IOException {

		if (balanceContext == null) {
			throw new IllegalArgumentException("null configuration");
		}

		synchronized (runLock) {

			if (running) {
				return;
			}

			this.balanceContext = balanceContext;

			this.balanceContext.getBalanceReverseAcceptor().start(reverseContext);

			this.channelAcceptor = new SocketChannelAcceptor(facadeContext);

			this.channelAcceptor.bind();

			LoggerUtil.prettyNIOServerLog(LoggerFactory.getLogger(BalanceFacadeAcceptor.class),
					"Balance Facade Acceptor 启动成功 ...");
		}

	}

	public void stop() {
		synchronized (runLock) {
			CloseUtil.unbind(channelAcceptor);
			this.balanceContext.getBalanceReverseAcceptor().stop();
		}
	}

	public BalanceContext getBalanceContext() {
		return balanceContext;
	}

	public SocketChannelAcceptor getAcceptor() {
		return channelAcceptor;
	}

}
