package com.generallycloud.nio.balance;

import com.generallycloud.nio.balance.router.BalanceRouter;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.SocketSEListenerAdapter;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.ReadFuture;

public class BalanceFacadeAcceptorSEListener extends SocketSEListenerAdapter {

	private Logger			logger	= LoggerFactory.getLogger(BalanceFacadeAcceptorSEListener.class);

	private BalanceContext	balanceContext;

	private BalanceRouter		balanceRouter;

	public BalanceFacadeAcceptorSEListener(BalanceContext balanceContext) {
		this.balanceContext = balanceContext;
		this.balanceRouter = balanceContext.getBalanceRouter();
	}

	@Override
	public void sessionOpened(SocketSession session) {
		balanceRouter.addClientSession((BalanceFacadeSocketSession) session);
		logger.info("客户端来自 [ {} ] 已建立连接.",session.getRemoteSocketAddress());
	}

	@Override
	public void sessionClosed(SocketSession session) {
		
		BalanceFacadeSocketSession fs = (BalanceFacadeSocketSession) session;

		balanceRouter.removeClientSession(fs);

		logger.info("客户端来自 [ {} ] 已断开连接.",session.getRemoteSocketAddress());

		BalanceRouter balanceRouter = balanceContext.getBalanceRouter();

		BalanceReverseSocketSession rs = balanceRouter.getRouterSession(fs);

		if (rs == null) {
			return;
		}

		ChannelLostReadFutureFactory factory = balanceContext.getChannelLostReadFutureFactory();

		if (factory == null) {
			return;
		}

		ReadFuture future = factory.createChannelLostPacket(session);

		rs.flush(future);
	}
}
