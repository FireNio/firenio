package com.generallycloud.nio.component;

import java.io.IOException;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.protocol.ReadFuture;

public class SessionActiveSEListener extends SEListenerAdapter {

	private Logger		logger	= LoggerFactory.getLogger(SessionActiveSEListener.class);

	public void sessionIdled(Session session, long lastIdleTime, long currentTime) {

		if (session.getLastAccessTime() < lastIdleTime) {

			CloseUtil.close(session);

		} else {

			NIOContext context = session.getContext();

			BeatFutureFactory factory = context.getBeatFutureFactory();

			if (factory == null) {

				RuntimeException e = new RuntimeException("none factory of BeatFuture");

				CloseUtil.close(session);

				logger.error(e.getMessage(), e);

				return;
			}

			ReadFuture future = factory.createPINGPacket(session);
			
			if (future == null) {
				// 该session无需心跳,比如HTTP协议
				return;
			}

			try {
				session.flush(future);
			} catch (IOException e) {
				CloseUtil.close(session);
				logger.error(e.getMessage(), e);
				return;
			}
		}
	}
}
