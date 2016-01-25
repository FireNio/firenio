package com.gifisan.mtp.server.session;

import com.gifisan.mtp.schedule.ServletAcceptJob;

public interface InnerSession extends Session {
	
	public abstract byte getSessionID();
	
	public abstract void destroyImmediately();
	
	public abstract ServletAcceptJob updateServletAcceptJob();
	
}
