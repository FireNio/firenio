package com.gifisan.nio.service;

import java.io.IOException;

import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.server.session.Session;

public interface WriteFuture {

	public abstract boolean write() throws IOException;

	public abstract EndPoint getEndPoint();
	
	public abstract Session getSession();

	public abstract boolean isNetworkWeak();

	public abstract void catchException(IOException e);
	
}
