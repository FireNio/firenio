package com.generallycloud.nio.front;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.SocketSEListenerAdapter;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.ReadFuture;

public class FrontFacadeAcceptorSEListener extends SocketSEListenerAdapter {

	private Logger			logger	= LoggerFactory.getLogger(FrontFacadeAcceptorSEListener.class);

	private FrontContext	balanceContext;

	private FrontRouter		frontRouter;

	public FrontFacadeAcceptorSEListener(FrontContext balanceContext) {
		this.balanceContext = balanceContext;
		this.frontRouter = balanceContext.getFrontRouter();
	}

	@Override
	public void sessionOpened(SocketSession session) {
		frontRouter.addClientSession((FrontFacadeSocketSession) session);
		logger.info("客户端来自 [ " + session.getRemoteSocketAddress() + " ] 已建立连接.");
	}

	@Override
	public void sessionClosed(SocketSession session) {

		FrontFacadeSocketSession fs = (FrontFacadeSocketSession) session;

		frontRouter.removeClientSession(fs);

		logger.info("客户端来自 [ " + session.getRemoteSocketAddress() + " ] 已断开连接.");
		
		ChannelLostReadFutureFactory factory = balanceContext.getChannelLostReadFutureFactory();
		
		if (factory == null) {
			return;
		}

		SocketSession rs = balanceContext.getBalanceFacadeConnector().getSession();

		if (rs == null) {
			return;
		}

		ReadFuture future = factory.createChannelLostPacket(session);

		rs.flush(future);
	}
}
