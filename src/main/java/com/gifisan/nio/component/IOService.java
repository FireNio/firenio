package com.gifisan.nio.component;

import com.gifisan.nio.server.NIOContext;

public interface IOService{

	public abstract NIOContext getContext() ;

	public abstract void setContext(NIOContext context);
	
}
