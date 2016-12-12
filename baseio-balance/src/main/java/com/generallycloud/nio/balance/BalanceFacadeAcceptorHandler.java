package com.generallycloud.nio.balance;

import com.generallycloud.nio.balance.router.BalanceRouter;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.IoEventHandleAdaptor;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.ReadFuture;

public class BalanceFacadeAcceptorHandler extends IoEventHandleAdaptor {

	private Logger			logger	= LoggerFactory.getLogger(BalanceFacadeAcceptorHandler.class);
	private BalanceRouter		balanceRouter;
	private byte[]		V		= {};

	public BalanceFacadeAcceptorHandler(BalanceContext context) {
		this.balanceRouter = context.getBalanceRouter();
	}

	public void accept(SocketSession session, ReadFuture future) throws Exception {
		
		BalanceFacadeSocketSession fs = (BalanceFacadeSocketSession) session;

		BalanceReadFuture f = (BalanceReadFuture) future;

		logger.info("报文来自客户端：[ {} ]，报文：{}", session.getRemoteSocketAddress(), f);

		//FIXME 是否需要设置取消接收广播
		if (f.isReceiveBroadcast()) {
			session.setAttribute(BalanceContext.BALANCE_RECEIVE_BROADCAST, V);
			return;
		}

		BalanceReverseSocketSession rs = balanceRouter.getRouterSession(fs, f);

		if (rs == null) {
			logger.info("未发现负载节点，报文分发失败：{} ", f);
			return;
		}
		
		f.setSessionID(session.getSessionID());

		f = f.translate();

		rs.flush(f);

		logger.info("分发请求到：[ {} ]", rs.getRemoteSocketAddress());
	}

}
