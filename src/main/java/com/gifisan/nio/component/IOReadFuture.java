package com.gifisan.nio.component;

import java.io.IOException;

public interface IOReadFuture extends ReadFuture{

	public abstract boolean read() throws IOException;

	public abstract EndPoint getEndPoint();
	
	public abstract Session getSession();

	public abstract void catchException(IOException e);
	
	public abstract boolean flushed();
	
	public abstract void flush();
	
}
