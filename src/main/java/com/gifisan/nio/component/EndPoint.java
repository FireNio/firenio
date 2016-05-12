package com.gifisan.nio.component;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;

import com.gifisan.nio.Attachment;
import com.gifisan.nio.server.NIOContext;

public interface EndPoint extends Closeable {

	public abstract void endConnect();

	public abstract String getLocalAddr();

	public abstract String getLocalHost();

	public abstract int getLocalPort();

	public abstract int getMaxIdleTime() throws SocketException;

	public abstract String getRemoteAddr();

	public abstract String getRemoteHost();

	public abstract int getRemotePort();

	public abstract boolean isBlocking();

	public abstract boolean isEndConnect();

	public abstract NIOContext getContext();
	
	public abstract void attach(Attachment attachment);

	public abstract Attachment attachment();

	public abstract Long getEndPointID();

	public abstract int read(ByteBuffer buffer) throws IOException;

	public abstract int write(ByteBuffer buffer) throws IOException;
}
