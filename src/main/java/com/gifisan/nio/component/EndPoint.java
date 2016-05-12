package com.gifisan.nio.component;

import java.io.Closeable;
import java.net.SocketException;

import com.gifisan.nio.Attachment;
import com.gifisan.nio.server.NIOContext;

public interface EndPoint extends Closeable {

	public abstract String getLocalAddr();

	public abstract String getLocalHost();

	public abstract int getLocalPort();

	public abstract int getMaxIdleTime() throws SocketException;

	public abstract String getRemoteAddr();

	public abstract String getRemoteHost();

	public abstract int getRemotePort();

	public abstract boolean isBlocking();

	public abstract NIOContext getContext();
	
	public abstract void attach(Attachment attachment);

	public abstract Attachment attachment();

	public abstract Long getEndPointID();

}
