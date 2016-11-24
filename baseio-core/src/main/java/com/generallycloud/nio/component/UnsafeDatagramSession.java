package com.generallycloud.nio.component;

public interface UnsafeDatagramSession extends DatagramSession{

	public abstract DatagramChannel getDatagramChannel();

	public abstract void fireOpend();

	public abstract void fireClosed();

	public abstract void physicalClose();
}
