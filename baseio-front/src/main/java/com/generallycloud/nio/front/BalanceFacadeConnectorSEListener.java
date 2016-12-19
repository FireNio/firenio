package com.generallycloud.nio.front;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.SocketSEListenerAdapter;
import com.generallycloud.nio.component.SocketSession;

public class BalanceFacadeConnectorSEListener extends SocketSEListenerAdapter {

	private Logger			logger	= LoggerFactory.getLogger(BalanceFacadeConnectorSEListener.class);

	@Override
	public void sessionOpened(SocketSession session) {
		logger.info("已连接到负载服务器 {}", session);
	}

	@Override
	public void sessionClosed(SocketSession session) {
		logger.info("与负载服务器 {} 已断开连接.",session);
	}
}
