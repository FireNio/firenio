package com.generallycloud.nio.component;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.protocol.ChannelReadFuture;
import com.generallycloud.nio.protocol.ReadFuture;

public abstract class AbstractReadFutureAcceptor implements ReadFutureAcceptor{

	private Logger logger = LoggerFactory.getLogger(AbstractReadFutureAcceptor.class);
	
	public void accept(final SocketSession session, final ReadFuture future) throws Exception {

		ChannelReadFuture f = (ChannelReadFuture) future;
		
		if (f.isSilent()) {
			return;
		}

		if (f.isHeartbeat()) {

			acceptHeartBeat(session, f);

			return;
		}
		
		SocketChannelContext context = session.getContext();

		IoEventHandle eventHandle = context.getIoEventHandleAdaptor();
		
		accept(eventHandle, session, f);
	}
	
	protected abstract void accept(IoEventHandle eventHandle,SocketSession session, ChannelReadFuture future);
	
	private void acceptHeartBeat(final SocketSession session, final ChannelReadFuture future) {

		if (future.isPING()) {

			logger.info("收到心跳请求!来自：{}", session);

			SocketChannelContext context = session.getContext();

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
