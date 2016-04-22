package com.gifisan.nio.server.session;

import java.net.SocketException;

import com.gifisan.nio.Attachment;
import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.server.NIOContext;

public interface Session {

	public abstract void attach(Attachment attachment);

	public abstract Attachment attachment();

	public abstract void disconnect();

	public abstract long getCreationTime();

	public abstract NIOContext getContext();
	
	public abstract EndPoint getEndPoint();

	public abstract String getLocalAddr();

	public abstract String getLocalHost();

	public abstract int getLocalPort();

	public abstract int getMaxIdleTime() throws SocketException;

	public abstract String getRemoteAddr();

	public abstract String getRemoteHost();

	public abstract int getRemotePort();

	public abstract boolean isBlocking();

	public abstract boolean isOpened();

	public abstract byte getSessionID();

}