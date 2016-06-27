package com.gifisan.nio.front;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.SessionEventListener;

public class FrontProxySEListener implements SessionEventListener{

	private Logger logger = LoggerFactory.getLogger(FrontProxySEListener.class);
	
	private RouterProxy		routerProxy;

	public FrontProxySEListener(RouterProxy routerProxy) {
		this.routerProxy = routerProxy;
	}

	public void sessionOpened(Session session) {
		logger.info("负载服务器来自 [ " + session.getRemoteSocketAddress() + " ] 已建立连接.");
		routerProxy.addSession(session);
	}

	public void sessionClosed(Session session) {
		logger.info("负载服务器来自 [ " + session.getRemoteSocketAddress() + " ] 已断开连接.");
		routerProxy.remove(session);
	}
}
