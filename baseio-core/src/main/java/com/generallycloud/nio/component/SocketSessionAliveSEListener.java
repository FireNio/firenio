package com.generallycloud.nio.component;

import com.generallycloud.nio.common.CloseUtil;

public class SocketSessionAliveSEListener extends SocketSEListenerAdapter{

	@Override
	public void sessionIdled(SocketSession session, long lastIdleTime, long currentTime) {
		
		if (session.getLastAccessTime() < lastIdleTime) {
			
			CloseUtil.close(session);
		}
	}
}
