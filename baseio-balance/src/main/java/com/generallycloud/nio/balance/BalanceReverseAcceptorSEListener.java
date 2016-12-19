package com.generallycloud.nio.balance;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.SocketSEListenerAdapter;
import com.generallycloud.nio.component.SocketSession;

public class BalanceReverseAcceptorSEListener extends SocketSEListenerAdapter {

	private Logger			logger	= LoggerFactory.getLogger(BalanceReverseAcceptorSEListener.class);

	private BalanceContext	context;

	public BalanceReverseAcceptorSEListener(BalanceContext context) {
		this.context = context;
	}

	@Override
	public void sessionOpened(SocketSession session) {
		logger.info("负载服务器来自 [ {} ] 已建立连接.", session);
		context.getBalanceRouter().addRouterSession((BalanceReverseSocketSession) session);
	}

	@Override
	public void sessionClosed(SocketSession session) {
		logger.info("负载服务器来自 [ {} ] 已断开连接.", session);
		context.getBalanceRouter().removeRouterSession((BalanceReverseSocketSession) session);
	}
}
