package com.generallycloud.nio.component;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.IOEventHandle.IOEventState;
import com.generallycloud.nio.component.concurrent.EventLoop;
import com.generallycloud.nio.component.protocol.IOReadFuture;

public class IOReadFutureDispatcher implements IOReadFutureAcceptor {

	private final Logger	logger	= LoggerFactory.getLogger(IOReadFutureDispatcher.class);

	public void accept(final Session session, final IOReadFuture future) throws Exception {

		if (future.isBeatPacket()) {

			if (!session.getContext().isAcceptBeat()) {
				
				logger.info("收到心跳回报!来自：{}",session);
				
				return;
			}
			
			logger.info("收到心跳请求!来自：{}",session);

			session.flush(future);

			return;
		}

		EventLoop eventLoop = session.getEventLoop();
		
		eventLoop.dispatch(new Runnable() {
			
			public void run() {
				
				NIOContext context = session.getContext();
				
				IOEventHandle eventHandle = context.getIOEventHandleAdaptor();

				try {

					eventHandle.accept(session, future);

				} catch (Exception e) {

					logger.error(e.getMessage(), e);

					eventHandle.exceptionCaught(session, future, e, IOEventState.HANDLE);
				}
			}
		});
	}
}
