package com.gifisan.nio.component;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

public interface EndPoint extends OutputStream, Closeable {

	public abstract int read(ByteBuffer buffer) throws IOException;

	public abstract int write(ByteBuffer buffer) throws IOException;

	public abstract void completedRead(ByteBuffer buffer) throws IOException;

	public abstract void completedWrite(ByteBuffer buffer) throws IOException;

	public abstract ByteBuffer read(int limit) throws IOException;

	public abstract int sessionSize();

	public abstract String getLocalAddr();

	public abstract String getLocalHost();

	public abstract int getLocalPort();

	public abstract int getMaxIdleTime();

	public abstract String getRemoteAddr();

	public abstract String getRemoteHost();

	public abstract int getRemotePort();

	public abstract void setInputStream(InputStream inputStream);

	public abstract boolean inStream();

	public abstract boolean isBlocking();

	public abstract boolean isOpened();

}
