package com.gifisan.nio.component;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

public interface EndPoint extends Closeable {

	public abstract void endConnect();

	public abstract String getLocalAddr();

	public abstract String getLocalHost();

	public abstract int getLocalPort();

	public abstract int getMaxIdleTime();

	public abstract String getRemoteAddr();

	public abstract String getRemoteHost();

	public abstract int getRemotePort();

	public abstract boolean inStream();

	public abstract boolean isBlocking();

	public abstract boolean isOpened();

	public abstract int sessionSize();

	public abstract InputStream getInputStream();

	public abstract void setInputStream(InputStream inputStream);
	
	public abstract ByteBuffer completedRead(int limit) throws IOException;
	
	public abstract void completedRead(ByteBuffer buffer) throws IOException;
	
	public abstract void completedWrite(ByteBuffer buffer) throws IOException;

	public abstract int read(ByteBuffer buffer) throws IOException;

	public abstract int write(ByteBuffer buffer) throws IOException;
	
	

}
