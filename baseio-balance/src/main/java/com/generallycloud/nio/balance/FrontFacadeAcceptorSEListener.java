package com.generallycloud.nio.balance;

import com.generallycloud.nio.balance.router.FrontRouter;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.SEListenerAdapter;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.ReadFuture;

public class FrontFacadeAcceptorSEListener extends SEListenerAdapter {

	private Logger			logger	= LoggerFactory.getLogger(FrontFacadeAcceptorSEListener.class);

	private FrontContext	frontContext;

	private FrontRouter		frontRouter;

	public FrontFacadeAcceptorSEListener(FrontContext frontContext) {
		this.frontContext = frontContext;
		this.frontRouter = frontContext.getFrontRouter();
	}

	public void sessionOpened(Session session) {
		frontRouter.addClientSession((SocketSession) session);
		logger.info("客户端来自 [ " + session.getRemoteSocketAddress() + " ] 已建立连接.");
	}

	public void sessionClosed(Session session) {

		frontRouter.removeClientSession((SocketSession) session);

		logger.info("客户端来自 [ " + session.getRemoteSocketAddress() + " ] 已断开连接.");

		FrontRouter frontRouter = frontContext.getFrontRouter();

		SocketSession router = frontRouter.getRouterSession((SocketSession) session);

		if (router == null) {

			return;
		}

		ChannelLostReadFutureFactory factory = frontContext.getChannelLostReadFutureFactory();

		if (factory == null) {
			return;
		}

		ReadFuture future = factory.createChannelLostPacket(session);

		router.flush(future);
	}
}
