package com.generallycloud.nio.component;

public interface SessionFactory {

	public abstract UnsafeSession newUnsafeSession(SocketChannel channel);
	
}
