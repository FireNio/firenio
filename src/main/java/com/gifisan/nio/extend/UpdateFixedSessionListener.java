package com.gifisan.nio.extend;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.SessionEventListener;

public class UpdateFixedSessionListener implements SessionEventListener{

	private FixedSession fixedSession = null;
	
	public UpdateFixedSessionListener(FixedSession fixedSession) {
		this.fixedSession = fixedSession;
	}

	public void sessionOpened(Session session) {
		fixedSession.update(session);
	}
	
	public void sessionClosed(Session session) {
		
	}

	
}
