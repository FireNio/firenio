package com.gifisan.nio.front;

import java.io.IOException;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.ReadFutureFactory;
import com.gifisan.nio.component.SEListenerAdapter;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.ReadFuture;

public class FrontFacadeAcceptorSEListener extends SEListenerAdapter {

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
		
		router.removeAttribute(session.getSessionID());

		ReadFuture future = ReadFutureFactory.create(session, sessionID, FrontContext.FRONT_CHANNEL_LOST, session
				.getContext().getIOEventHandleAdaptor());

		future.write(session.toString());

		try {
			router.flush(future);
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
		}
	}
}
