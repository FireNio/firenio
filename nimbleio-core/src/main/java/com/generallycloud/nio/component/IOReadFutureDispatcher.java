package com.generallycloud.nio.component;

import java.io.IOException;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.IOEventHandle.IOEventState;
import com.generallycloud.nio.component.concurrent.EventLoop;
import com.generallycloud.nio.component.protocol.IOReadFuture;
import com.generallycloud.nio.component.protocol.ReadFuture;

public class IOReadFutureDispatcher implements IOReadFutureAcceptor {

	private final Logger	logger	= LoggerFactory.getLogger(IOReadFutureDispatcher.class);

	public void accept(final Session session, final IOReadFuture future) throws Exception {

		if (future.isHeartbeat()) {

			acceptHeartBeat(session, future);

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

	private void acceptHeartBeat(final Session session, final IOReadFuture future) {

		if (future.isPING()) {

			logger.info("收到心跳请求!来自：{}", session);
			
			NIOContext context = session.getContext();

			BeatFutureFactory factory = context.getBeatFutureFactory();

			if (factory == null) {

				RuntimeException e = new RuntimeException("none factory of BeatFuture");

				CloseUtil.close(session);

				logger.error(e.getMessage(), e);

				return;
			}

			ReadFuture f = factory.createPONGPacket(session);

			try {
				session.flush(f);
			} catch (IOException e) {
				CloseUtil.close(session);
				logger.error(e.getMessage(), e);
				return;
			}
		}else{
			logger.info("收到心跳回报!来自：{}", session);
		}

	}
}
