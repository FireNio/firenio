package com.generallycloud.nio.component;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;

public class LoggerDatagramSEListener implements DatagramSessionEventListener{

	private Logger logger = LoggerFactory.getLogger(LoggerDatagramSEListener.class);
	
	@Override
	public void sessionOpened(DatagramSession session) {
		logger.info("session opened:{}",session);
	}

	@Override
	public void sessionClosed(DatagramSession session) {
		logger.info("session closed:{}",session);
	}

	@Override
	public void sessionIdled(DatagramSession session,long lastIdleTime, long currentTime) {
//		logger.debug("session Idled:{}",session);
	}
}
