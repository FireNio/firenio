package com.gifisan.nio.component;

import java.net.SocketException;

import com.gifisan.nio.Attachment;
import com.gifisan.nio.server.NIOContext;
import com.gifisan.nio.service.ServiceAcceptor;

public interface Session {
	
	public static byte SESSION_ID_1 = 0;
	
	public static byte SESSION_ID_2 = 1;
	
	public static byte SESSION_ID_3 = 2;
	
	public static byte SESSION_ID_4 = 3;
	
	public abstract ServiceAcceptor getServiceAcceptor();

	public abstract void addEventListener(SessionEventListener listener);
	
	public abstract void attach(Attachment attachment);

	public abstract Attachment attachment();

	public abstract void disconnect();

	public abstract long getCreationTime();

	public abstract NIOContext getContext();
	
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
	
	public abstract void destroyImmediately();

}