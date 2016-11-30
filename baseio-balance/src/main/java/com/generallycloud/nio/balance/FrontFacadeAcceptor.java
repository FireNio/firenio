package com.generallycloud.nio.balance;

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

	public void start(FrontContext frontContext, SocketChannelContext frontBaseContext, SocketChannelContext frontReverseBaseContext)
			throws IOException {

		if (frontContext == null) {
			throw new IllegalArgumentException("null configuration");
		}
		
		synchronized (runLock) {
			
			if (running) {
				return;
			}
			
			this.frontContext = frontContext;

			this.frontContext.getFrontReverseAcceptor().start(frontReverseBaseContext);

			this.acceptor = new SocketChannelAcceptor(frontBaseContext);

			this.acceptor.bind();

			LoggerUtil.prettyNIOServerLog(LoggerFactory.getLogger(FrontFacadeAcceptor.class),
					"Front Facade Acceptor 启动成功 ...");
		}

	}

	public void stop() {
		synchronized (runLock) {
			CloseUtil.unbind(acceptor);
			this.frontContext.getFrontReverseAcceptor().stop();
		}
	}

	public FrontContext getFrontContext() {
		return frontContext;
	}

	public SocketChannelAcceptor getAcceptor() {
		return acceptor;
	}

}
