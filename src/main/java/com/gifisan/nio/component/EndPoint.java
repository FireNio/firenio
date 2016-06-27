package com.gifisan.nio.component;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.net.SocketException;

import com.gifisan.nio.Attachment;

public interface EndPoint extends Closeable {

	public abstract String getLocalAddr();

	public abstract String getLocalHost();

	public abstract int getLocalPort();

	public abstract String getRemoteAddr();

	public abstract String getRemoteHost();

	public abstract int getRemotePort();

	public abstract NIOContext getContext();
	
	public abstract void attach(Attachment attachment);

	public abstract Attachment attachment();
	
	public abstract InetSocketAddress getLocalSocketAddress() ;
	
	public abstract Session getSession();

	public abstract Integer getEndPointID();
	
	public abstract InetSocketAddress getRemoteSocketAddress();

	public abstract int getMaxIdleTime() throws SocketException;

}
