package com.gifisan.nio.extend;

import java.io.IOException;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.SEListenerAdapter;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.extend.implementation.SYSTEMBeatPacketServlet;

public class SessionActiveSEListener extends SEListenerAdapter {

	private FixedSession	fixedSession	= null;
	
	public SessionActiveSEListener(FixedSession fixedSession) {
		this.fixedSession = fixedSession;
	}

	private Logger			logger		= LoggerFactory.getLogger(SessionActiveSEListener.class);

	public void sessionIdled(Session session, long lastIdleTime, long currentTime) {

		try {

			if (fixedSession == null) {
				return;
			}

			fixedSession.request(SYSTEMBeatPacketServlet.SERVICE_NAME, null);

			logger.debug("收到心跳回报!");

		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
}
