package com.generallycloud.nio.component;

public interface UnsafeSession extends SocketSession{

	public abstract SocketChannel getSocketChannel();
	
}
