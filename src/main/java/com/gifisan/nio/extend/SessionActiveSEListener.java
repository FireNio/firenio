package com.gifisan.nio.extend;

import java.io.IOException;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.SEListenerAdapter;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.concurrent.Waiter;
import com.gifisan.nio.component.protocol.future.ReadFuture;

public abstract class SessionActiveSEListener extends SEListenerAdapter {

	public static final String	SESSION_ACTIVE_BEAT	= "_SESSION_ACTIVE_BEAT";

	public static final String	SESSION_ACTIVE_WAITER	= "_SESSION_ACTIVE_WAITER";

	private Logger				logger				= LoggerFactory.getLogger(SessionActiveSEListener.class);

	public void sessionIdled(Session session, long lastIdleTime, long currentTime) {

		ReadFuture future = getBeatPacket(session);

		Waiter<ReadFuture> waiter = new Waiter<ReadFuture>();

		session.setAttribute(SESSION_ACTIVE_WAITER, waiter);

		try {
			session.flush(future);
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
			return;
		}

		if (waiter.await(3000)) {
			CloseUtil.close(session);
		} else {
			logger.info("收到心跳回报");
		}
	}

	protected abstract ReadFuture getBeatPacket(Session session);
}
