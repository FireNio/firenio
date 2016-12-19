package com.generallycloud.nio.component;

import com.generallycloud.nio.AbstractLinkable;

public class SocketSEListenerWrapper extends AbstractLinkable<SocketSessionEventListener> implements SocketSessionEventListener {

	public SocketSEListenerWrapper(SocketSessionEventListener value) {
		super(value);
	}

	@Override
	public void sessionOpened(SocketSession session) {
		getValue().sessionOpened(session);
	}

	@Override
	public void sessionClosed(SocketSession session) {
		getValue().sessionClosed(session);

	}

	@Override
	public void sessionIdled(SocketSession session, long lastIdleTime, long currentTime) {
		getValue().sessionIdled(session, lastIdleTime, currentTime);
	}

}
