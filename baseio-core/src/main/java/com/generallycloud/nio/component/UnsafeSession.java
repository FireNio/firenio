package com.generallycloud.nio.component;

public interface UnsafeSession extends SocketSession{

	public abstract SocketChannel getSocketChannel();

	public abstract void fireOpend();

	public abstract void fireClosed();

	public abstract void physicalClose();
	
}
