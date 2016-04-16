package com.gifisan.nio.server;

import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.server.session.InnerSession;
import com.gifisan.nio.service.Response;

public interface InnerResponse extends Response{

	public abstract boolean flushed();
	
	public abstract boolean schduled();
	
	public abstract EndPoint getEndPoint();

	public abstract byte getSessionID();

	public abstract InnerSession getInnerSession();
	
}
