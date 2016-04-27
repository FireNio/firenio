package com.gifisan.nio.component;

import com.gifisan.nio.component.future.IOReadFuture;

public interface OutputStreamAcceptor {
	
	public abstract void accept(Session session, IOReadFuture future) throws Exception;

}
