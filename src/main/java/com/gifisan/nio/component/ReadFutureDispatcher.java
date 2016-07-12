package com.gifisan.nio.component;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.protocol.future.ReadFuture;

public class ReadFutureDispatcher implements ReadFutureAcceptor {

	private final Logger	logger	= LoggerFactory.getLogger(ReadFutureDispatcher.class);

	public void accept(Session session, ReadFuture future) {

		NIOContext context = session.getContext();

		IOEventHandle eventHandle = context.getIOEventHandleAdaptor();

		try {
			eventHandle.accept(session, future);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

}
