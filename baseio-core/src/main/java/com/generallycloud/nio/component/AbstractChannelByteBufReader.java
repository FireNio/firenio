package com.generallycloud.nio.component;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.IOEventHandle.IOEventState;
import com.generallycloud.nio.component.concurrent.EventLoop;
import com.generallycloud.nio.protocol.ChannelReadFuture;
import com.generallycloud.nio.protocol.ReadFuture;

public abstract class AbstractChannelByteBufReader implements ChannelByteBufReader{

	private Logger logger = LoggerFactory.getLogger(AbstractChannelByteBufReader.class);
	
	protected ByteBuf allocate(Session session,int capacity){
		return session.getByteBufAllocator().allocate(capacity);
	}
	
	protected void accept(final Session session, final ChannelReadFuture future) throws Exception {

		if (future.isSilent()) {
			return;
		}

		if (future.isHeartbeat()) {

			acceptHeartBeat(session, future);

			return;
		}

		EventLoop eventLoop = session.getEventLoop();

		eventLoop.dispatch(new Runnable() {

			public void run() {

				BaseContext context = session.getContext();

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

	private void acceptHeartBeat(final Session session, final ChannelReadFuture future) {

		if (future.isPING()) {

			logger.info("收到心跳请求!来自：{}", session);

			BaseContext context = session.getContext();

			BeatFutureFactory factory = context.getBeatFutureFactory();

			if (factory == null) {

				RuntimeException e = new RuntimeException("none factory of BeatFuture");

				CloseUtil.close(session);

				logger.error(e.getMessage(), e);

				return;
			}

			ReadFuture f = factory.createPONGPacket(session);

			session.flush(f);
		} else {
			logger.info("收到心跳回报!来自：{}", session);
		}

	}
}
