package com.gifisan.nio.connector;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.IOEventHandle;
import com.gifisan.nio.component.IOReadFutureAcceptor;
import com.gifisan.nio.component.NIOContext;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.IOReadFuture;

public class ClientIOReadFutureDispatcher implements IOReadFutureAcceptor {

	private final Logger	logger	= LoggerFactory.getLogger(ClientIOReadFutureDispatcher.class);

	public void accept(Session session, IOReadFuture future) throws Exception {
		
		if (future.isBeatPacket()) {
			
			logger.debug("收到心跳回报!");
			
			return;
		}

		NIOContext context = session.getContext();

		IOEventHandle eventHandle = context.getIOEventHandleAdaptor();

		eventHandle.accept(session, future);
	}
}
