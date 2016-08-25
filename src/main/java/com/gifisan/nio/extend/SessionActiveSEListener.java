package com.gifisan.nio.extend;

import java.io.IOException;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.SEListenerAdapter;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.future.ReadFuture;

public abstract class SessionActiveSEListener extends SEListenerAdapter {

	private Logger				logger				= LoggerFactory.getLogger(SessionActiveSEListener.class);

	public void sessionIdled(Session session, long lastIdleTime, long currentTime) {

		if (session.getLastAccessTime() < lastIdleTime) {
			
			CloseUtil.close(session);
			
		}else{
			
			ReadFuture future = getBeatPacket(session);
			
			try {
				session.flush(future);
			} catch (IOException e) {
				CloseUtil.close(session);
				logger.error(e.getMessage(),e);
				return;
			}
		}
	}

	protected abstract ReadFuture getBeatPacket(Session session);
}
