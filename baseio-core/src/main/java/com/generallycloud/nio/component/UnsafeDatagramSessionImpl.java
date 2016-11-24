package com.generallycloud.nio.component;

public class UnsafeDatagramSessionImpl extends DatagramSessionImpl implements UnsafeDatagramSession{

	public UnsafeDatagramSessionImpl(DatagramChannel channel, Integer sessionID) {
		super(channel, sessionID);
	}

	public DatagramChannel getDatagramChannel() {
		return channel;
	}

	public void fireOpend() {
		throw new UnsupportedOperationException();
	}

	public void fireClosed() {
		throw new UnsupportedOperationException();
		
	}

	public void physicalClose() {
		throw new UnsupportedOperationException();
	}
	
}
