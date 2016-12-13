package com.generallycloud.nio.front;

import java.io.IOException;

import com.generallycloud.nio.acceptor.SocketChannelAcceptor;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.LoggerUtil;
import com.generallycloud.nio.component.SocketChannelContext;

public class FrontFacadeAcceptor {

	private byte[]				runLock				= new byte[]{};
	private boolean				running				= false;
	private SocketChannelAcceptor		acceptor				= null;
	private FrontContext			frontContext;

	public void start(FrontContext frontContext, SocketChannelContext socketChannelContext, SocketChannelContext balanceReverseChannelContext)
			throws IOException {

		if (frontContext == null) {
			throw new IllegalArgumentException("null configuration");
		}
		
		synchronized (runLock) {
			
			if (running) {
				return;
			}
			
			this.frontContext = frontContext;

			this.frontContext.getBalanceFacadeConnector().connect(balanceReverseChannelContext);

			this.acceptor = new SocketChannelAcceptor(socketChannelContext);

			this.acceptor.bind();

			LoggerUtil.prettyNIOServerLog(LoggerFactory.getLogger(FrontFacadeAcceptor.class),
					"Balance Facade Acceptor 启动成功 ...");
		}

	}

	public void stop() {
		synchronized (runLock) {
			CloseUtil.unbind(acceptor);
			CloseUtil.close(frontContext.getBalanceFacadeConnector());
		}
	}

	public FrontContext getBalanceContext() {
		return frontContext;
	}

	public SocketChannelAcceptor getAcceptor() {
		return acceptor;
	}

}
