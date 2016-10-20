package com.generallycloud.nio.component;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;

public class LoggerSEListener implements SessionEventListener{

	private Logger logger = LoggerFactory.getLogger(LoggerSEListener.class);
	
	public void sessionOpened(Session session) {
		logger.debug("session opened:{}",session);
	}

	public void sessionClosed(Session session) {
		logger.debug("session closed:{}",session);
	}

	public void sessionIdled(Session session,long lastIdleTime, long currentTime) {
		logger.debug("session Idled:{}",session);
	}
}
