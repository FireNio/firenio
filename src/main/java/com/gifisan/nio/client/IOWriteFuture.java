package com.gifisan.nio.client;

import java.io.IOException;

import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.WriteFuture;

public interface IOWriteFuture extends WriteFuture{

	public abstract boolean write() throws IOException;

	public abstract EndPoint getEndPoint();
	
	public abstract Session getSession();

	public abstract boolean isNetworkWeak();

	public abstract void catchException(IOException e);
	
	public abstract long getFutureID();
	
}
