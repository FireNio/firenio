package com.gifisan.nio.component;

public interface OutputStreamAcceptor {
	
	public abstract void accept(Session session, IOReadFuture future) throws Exception;

}
