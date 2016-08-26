package com.gifisan.nio.acceptor;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.IOEventHandle;
import com.gifisan.nio.component.IOReadFutureAcceptor;
import com.gifisan.nio.component.NIOContext;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.IOReadFuture;

public class ServerIOReadFutureDispatcher implements IOReadFutureAcceptor {

	private final Logger	logger	= LoggerFactory.getLogger(ServerIOReadFutureDispatcher.class);

	public void accept(Session session, IOReadFuture future) throws Exception {

		if (future.isBeatPacket()) {

			logger.debug("收到心跳请求!");

//			NIOContext context = session.getContext();

//			BeatFutureFactory factory = context.getBeatFutureFactory();
//
//			if (factory == null) {
//				throw new IOException("none factory of BeatFuture");
//			}
//
//			ReadFuture f = factory.createBeatPacket(session);

			session.flush(future);

			return;
		}

		NIOContext context = session.getContext();

		IOEventHandle eventHandle = context.getIOEventHandleAdaptor();

		eventHandle.accept(session, future);
	}
}
