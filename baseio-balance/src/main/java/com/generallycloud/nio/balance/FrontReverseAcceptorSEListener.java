package com.generallycloud.nio.balance;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.IOSession;
import com.generallycloud.nio.component.SEListenerAdapter;
import com.generallycloud.nio.component.Session;

public class FrontReverseAcceptorSEListener extends SEListenerAdapter {

	private Logger			logger	= LoggerFactory.getLogger(FrontReverseAcceptorSEListener.class);

	private FrontContext	context;

	public FrontReverseAcceptorSEListener(FrontContext context) {
		this.context = context;
	}

	public void sessionOpened(Session session) {
		logger.info("负载服务器来自 " + session + " 已建立连接.");
		context.getFrontRouter().addRouterSession((IOSession) session);
	}

	public void sessionClosed(Session session) {
		logger.info("负载服务器来自 " + session + " 已断开连接.");
		context.getFrontRouter().removeRouterSession((IOSession) session);
	}
}
