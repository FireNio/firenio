package com.generallycloud.nio.balancing;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.IOSession;
import com.generallycloud.nio.component.SEListenerAdapter;
import com.generallycloud.nio.component.Session;

public class FrontFacadeAcceptorSEListener extends SEListenerAdapter {

	private Logger				logger	= LoggerFactory.getLogger(FrontFacadeAcceptorSEListener.class);

	private FrontRouterMapping	routerProxy;

	public FrontFacadeAcceptorSEListener(FrontRouterMapping routerProxy) {
		this.routerProxy = routerProxy;
	}

	public void sessionOpened(Session session) {
		logger.info("客户端来自 [ " + session.getRemoteSocketAddress() + " ] 已建立连接.");
	}

	//FIXME 客户端离线后通知负载均衡机器
	public void sessionClosed(Session session) {

		logger.info("客户端来自 [ " + session.getRemoteSocketAddress() + " ] 已断开连接.");

//		Integer sessionID = session.getSessionID();

		IOSession router = routerProxy.getRouterSession((IOSession) session);

		if (router == null) {

			return;
		}
		
		router.removeAttribute(session.getSessionID());

//		ReadFuture future = ReadFutureFactory.create(session, sessionID, FrontContext.FRONT_CHANNEL_LOST, session
//				.getContext().getIOEventHandleAdaptor());
//
//		future.write(session.toString());
//
//		try {
//			router.flush(future);
//		} catch (IOException e) {
//			logger.error(e.getMessage(),e);
//		}
	}
}
