package com.generallycloud.nio.component;

import com.generallycloud.nio.AbstractLinkable;

public class SessionEventListenerWrapper extends AbstractLinkable<SessionEventListener> implements SessionEventListener {

	public SessionEventListenerWrapper(SessionEventListener value) {
		super(value);
	}

	public void sessionOpened(Session session) {
		getValue().sessionOpened(session);
	}

	public void sessionClosed(Session session) {
		getValue().sessionClosed(session);

	}

	public void sessionIdled(Session session, long lastIdleTime, long currentTime) {
		getValue().sessionIdled(session, lastIdleTime, currentTime);
	}

}
