package com.gifisan.nio.component;

import java.io.IOException;

import com.gifisan.nio.component.future.WriteFuture;

public interface IOWriteFuture extends WriteFuture{

	public abstract boolean write() throws IOException;

	public abstract EndPoint getEndPoint();
	
	public abstract Session getSession();

	public abstract boolean isNetworkWeak();

	public abstract void onException(IOException e);
	
	public abstract void onSuccess();
	
	public abstract long getFutureID();
	
}
