package com.generallycloud.nio.component;

import com.generallycloud.nio.AbstractLinkable;

public class DatagramSEListenerWrapper extends AbstractLinkable<DatagramSessionEventListener> implements DatagramSessionEventListener {

	public DatagramSEListenerWrapper(DatagramSessionEventListener value) {
		super(value);
	}

	public void sessionOpened(DatagramSession session) {
		getValue().sessionOpened(session);
	}

	public void sessionClosed(DatagramSession session) {
		getValue().sessionClosed(session);

	}

	public void sessionIdled(DatagramSession session, long lastIdleTime, long currentTime) {
		getValue().sessionIdled(session, lastIdleTime, currentTime);
	}

}
