package com.generallycloud.nio.component;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.protocol.ChannelReadFuture;
import com.generallycloud.nio.protocol.ReadFuture;

public abstract class AbstractReadFutureAcceptor implements ReadFutureAcceptor{

	private Logger logger = LoggerFactory.getLogger(AbstractChannelByteBufReader.class);
	
	public void accept(final Session session, final ReadFuture future) throws Exception {

		ChannelReadFuture f = (ChannelReadFuture) future;
		
		if (f.isSilent()) {
			return;
		}

		if (f.isHeartbeat()) {

			acceptHeartBeat(session, f);

			return;
		}
		
		BaseContext context = session.getContext();

		IOEventHandle eventHandle = context.getIOEventHandleAdaptor();
		
		accept(eventHandle, session, f);
	}
	
	protected abstract void accept(IOEventHandle eventHandle,Session session, ChannelReadFuture future);
	
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
