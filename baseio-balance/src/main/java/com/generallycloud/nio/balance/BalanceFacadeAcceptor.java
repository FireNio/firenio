package com.generallycloud.nio.balance;

import java.io.IOException;

import com.generallycloud.nio.acceptor.SocketChannelAcceptor;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.LoggerUtil;
import com.generallycloud.nio.component.SocketChannelContext;

public class BalanceFacadeAcceptor {

	private byte[]				runLock				= new byte[]{};
	private boolean				running				= false;
	private SocketChannelAcceptor		acceptor				= null;
	private BalanceContext			balanceContext;

	public void start(BalanceContext balanceContext, SocketChannelContext balanceBaseContext, SocketChannelContext balanceReverseBaseContext)
			throws IOException {

		if (balanceContext == null) {
			throw new IllegalArgumentException("null configuration");
		}
		
		synchronized (runLock) {
			
			if (running) {
				return;
			}
			
			this.balanceContext = balanceContext;

			this.balanceContext.getBalanceReverseAcceptor().start(balanceReverseBaseContext);

			this.acceptor = new SocketChannelAcceptor(balanceBaseContext);

			this.acceptor.bind();

			LoggerUtil.prettyNIOServerLog(LoggerFactory.getLogger(BalanceFacadeAcceptor.class),
					"Balance Facade Acceptor 启动成功 ...");
		}

	}

	public void stop() {
		synchronized (runLock) {
			CloseUtil.unbind(acceptor);
			this.balanceContext.getBalanceReverseAcceptor().stop();
		}
	}

	public BalanceContext getBalanceContext() {
		return balanceContext;
	}

	public SocketChannelAcceptor getAcceptor() {
		return acceptor;
	}

}
