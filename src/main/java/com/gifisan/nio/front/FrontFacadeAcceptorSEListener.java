package com.gifisan.nio.front;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.ReadFutureFactory;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.SessionEventListener;
import com.gifisan.nio.component.protocol.future.ReadFuture;

public class FrontFacadeAcceptorSEListener implements SessionEventListener {

	private Logger				logger	= LoggerFactory.getLogger(FrontFacadeAcceptorSEListener.class);

	private FrontRouterMapping	routerProxy;

	public FrontFacadeAcceptorSEListener(FrontRouterMapping routerProxy) {
		this.routerProxy = routerProxy;
	}

	public void sessionOpened(Session session) {
		logger.info("客户端来自 [ " + session.getRemoteSocketAddress() + " ] 已建立连接.");
	}

	public void sessionClosed(Session session) {

		logger.info("客户端来自 [ " + session.getRemoteSocketAddress() + " ] 已断开连接.");

		Integer sessionID = session.getSessionID();

		Session router = routerProxy.getRouterSession(session);

		if (router == null) {

			return;
		}

		ReadFuture future = ReadFutureFactory.create(session, sessionID, FrontContext.FRONT_CHANNEL_LOST, session
				.getContext().getIOEventHandleAdaptor());

		future.write(session.toString());

		router.flush(future);
	}
}
