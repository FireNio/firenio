package com.gifisan.nio.server.session;

import com.gifisan.nio.schedule.ServletAcceptJob;

public interface InnerSession extends Session {
	
	public abstract byte getSessionID();
	
	public abstract void destroyImmediately();
	
	public abstract ServletAcceptJob updateServletAcceptJob();
	
}
