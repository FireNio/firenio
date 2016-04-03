package com.gifisan.nio.server;

import java.io.IOException;

import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.server.session.InnerSession;

public interface InnerResponse extends Response{

	public abstract boolean flushed();
	
	public abstract boolean schduled();
	
	public abstract boolean complete();

	public abstract EndPoint getEndPoint();

	public abstract byte getSessionID();

	public abstract void doWrite() throws IOException;

	public abstract void catchException(Request request,Response response,IOException exception);

	public abstract InnerSession getInnerSession();
}
