package com.gifisan.nio.component;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.component.future.IOReadFuture;

public interface TCPEndPoint extends EndPoint{
	
	public abstract void endConnect();
	
	public abstract boolean isEndConnect();
	
	public abstract void setCurrentWriter(IOWriteFuture writer);
	
	public abstract IOWriteFuture getCurrentWriter();
	
	public abstract boolean isOpened();
	
	public abstract boolean isNetworkWeak();
	
	public abstract void attackNetwork(int length);
	
	public abstract void flushWriters() throws IOException ;
	
	public abstract Session getSession() throws IOException;
	
	public abstract IOReadFuture getReadFuture();
	
	public abstract void setReadFuture(IOReadFuture future);
	
	public abstract void incrementWriter();
	
	public abstract void decrementWriter();
	
	public abstract int read(ByteBuffer buffer) throws IOException;

	public abstract int write(ByteBuffer buffer) throws IOException;
	
	public abstract EndPointWriter getEndPointWriter();
	
	public abstract boolean isBlocking();
	
}
