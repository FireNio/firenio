package com.generallycloud.nio.balancing;

import java.io.IOException;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.IOSession;
import com.generallycloud.nio.component.SEListenerAdapter;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.protocol.ReadFuture;

public class FrontFacadeAcceptorSEListener extends SEListenerAdapter {

	private Logger				logger	= LoggerFactory.getLogger(FrontFacadeAcceptorSEListener.class);

	private FrontRouterMapping	routerProxy;

	private FrontContext		frontContext;

	public FrontFacadeAcceptorSEListener(FrontContext frontContext) {
		this.frontContext = frontContext;
		this.routerProxy = frontContext.getFrontRouterMapping();
	}

	public void sessionOpened(Session session) {
		logger.info("客户端来自 [ " + session.getRemoteSocketAddress() + " ] 已建立连接.");
	}

	public void sessionClosed(Session session) {

		logger.info("客户端来自 [ " + session.getRemoteSocketAddress() + " ] 已断开连接.");

		IOSession router = routerProxy.getRouterSession((IOSession) session);

		if (router == null) {

			return;
		}

		ChannelLostReadFutureFactory factory = frontContext.getChannelLostReadFutureFactory();

		if (factory == null) {
			return;
		}

		ReadFuture future = factory.createChannelLostPacket(session);

		try {
			router.flush(future);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
}
