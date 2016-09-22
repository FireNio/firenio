package com.generallycloud.nio.balancing;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.generallycloud.nio.acceptor.SocketChannelAcceptor;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.LoggerUtil;
import com.generallycloud.nio.component.NIOContext;

public class FrontFacadeAcceptor {

	private AtomicBoolean			started				= new AtomicBoolean(false);
	private SocketChannelAcceptor		acceptor				= new SocketChannelAcceptor();
	private FrontContext			frontContext;
	private FrontReverseAcceptor		frontReverseAcceptor	= new FrontReverseAcceptor();

	public void start(FrontContext frontContext, NIOContext frontNIOContext, NIOContext frontReverseNIOContext)
			throws IOException {

		if (frontContext == null) {
			throw new IllegalArgumentException("null configuration");
		}

		if (!started.compareAndSet(false, true)) {
			return;
		}

		this.frontContext = frontContext;

		this.frontReverseAcceptor.start(frontReverseNIOContext);

		this.acceptor.setContext(frontNIOContext);

		this.acceptor.bind();

		LoggerUtil.prettyNIOServerLog(LoggerFactory.getLogger(FrontFacadeAcceptor.class),
				"Front Facade Acceptor 启动成功 ...");
	}

	public void stop() {
		if (!started.get()) {
			return;
		}
		this.acceptor.unbind();

		this.frontReverseAcceptor.stop();
	}

	public FrontContext getFrontContext() {
		return frontContext;
	}

	public SocketChannelAcceptor getAcceptor() {
		return acceptor;
	}

}
