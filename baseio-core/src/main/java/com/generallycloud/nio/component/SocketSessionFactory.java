package com.generallycloud.nio.component;

public interface SocketSessionFactory {

	public abstract UnsafeSocketSession newUnsafeSession(SocketChannel channel);
	
}
