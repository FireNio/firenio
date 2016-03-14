package com.gifisan.nio.server.session;

import com.gifisan.nio.server.selector.ServiceAcceptor;

public interface InnerSession extends Session {
	
	public abstract byte getSessionID();
	
	public abstract void destroyImmediately();
	
	public abstract ServiceAcceptor updateServletAcceptJob();
	
}
