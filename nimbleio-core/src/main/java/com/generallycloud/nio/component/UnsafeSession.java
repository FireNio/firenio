package com.generallycloud.nio.component;

public interface UnsafeSession extends IOSession{

	public abstract SocketChannel getSocketChannel();
	
}
