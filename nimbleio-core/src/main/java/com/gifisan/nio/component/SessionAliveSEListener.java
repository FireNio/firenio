package com.gifisan.nio.component;

import com.gifisan.nio.common.CloseUtil;

public class SessionAliveSEListener extends SEListenerAdapter{

	public void sessionIdled(Session session, long lastIdleTime, long currentTime) {
		
		if (session.getLastAccessTime() < lastIdleTime) {
			
			CloseUtil.close(session);
		}
	}
}
