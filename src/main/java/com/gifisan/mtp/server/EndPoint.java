package com.gifisan.mtp.server;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

public interface EndPoint extends OutputStream, Closeable {
	
	public abstract void endConnect();

	public abstract String getLocalAddr();
	
	public abstract String getLocalHost();
	
	public abstract int getLocalPort();
	
	public abstract int getMaxIdleTime();
	
	public abstract String getRemoteAddr();
	
	public abstract String getRemoteHost();
	
	public abstract int getRemotePort();
	
	public abstract boolean isBlocking();
	
	public abstract boolean isOpened();
	
	public abstract boolean isEndConnect();
	
	public abstract ByteBuffer completeRead(int limit) throws IOException;
	
	public abstract ByteBuffer read(int limit) throws IOException;
	
	public abstract int read(ByteBuffer buffer) throws IOException;

    public abstract void write(ByteBuffer buffer) throws IOException;

}
