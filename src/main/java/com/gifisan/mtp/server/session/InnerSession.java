package com.gifisan.mtp.server.session;

import com.gifisan.mtp.schedule.ServletAcceptJob;

public interface InnerSession extends Session {
	
//	public abstract void activeDoor();

	public abstract void destroyImmediately();
	
	public abstract ServletAcceptJob updateServletAcceptJob();
	
}
