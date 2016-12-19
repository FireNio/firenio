package com.generallycloud.nio.component;

import com.generallycloud.nio.AbstractLifeCycle;
import com.generallycloud.nio.LifeCycle;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.protocol.ReadFuture;

public abstract class IoEventHandleAdaptor extends AbstractLifeCycle implements IoEventHandle, LifeCycle {

	private Logger		logger	= LoggerFactory.getLogger(IoEventHandleAdaptor.class);

	@Override
	public void exceptionCaught(SocketSession session, ReadFuture future, Exception cause, IoEventState state) {
		logger.error(cause.getMessage(),cause);
	}

	@Override
	public void futureSent(SocketSession session, ReadFuture future) {
		
	}

	@Override
	protected void doStart() throws Exception {

	}

	@Override
	protected void doStop() throws Exception {

	}

}
