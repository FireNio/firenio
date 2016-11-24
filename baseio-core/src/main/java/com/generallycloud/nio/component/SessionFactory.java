package com.generallycloud.nio.component;

public interface SessionFactory {

	public abstract UnsafeSocketSession newUnsafeSession(SocketChannel channel);
	
}
