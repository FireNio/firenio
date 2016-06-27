package com.gifisan.nio.front;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.SessionEventListener;

public class FrontFacadeSEListener implements SessionEventListener {

	private Logger		logger	= LoggerFactory.getLogger(FrontFacadeSEListener.class);

	public void sessionOpened(Session session) {
		logger.info("客户端来自 [ " + session.getRemoteSocketAddress() + " ] 已建立连接.");
	}

	public void sessionClosed(Session session) {
		logger.info("客户端来自 [ " + session.getRemoteSocketAddress() + " ] 已断开连接.");
	}
}
