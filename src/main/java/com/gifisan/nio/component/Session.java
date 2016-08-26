package com.gifisan.nio.component;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;

import com.gifisan.nio.component.concurrent.ReentrantMap;
import com.gifisan.nio.component.protocol.ReadFuture;
import com.gifisan.nio.extend.PluginContext;

public interface Session extends Closeable{

	public abstract void active();
	
	public abstract void clearAttributes() ;

	public abstract boolean closed();

	/**
	 * 该方法为非线程安全，Connector端使用时应注意
	 */
	public abstract void close();

	public abstract void flush(ReadFuture future) throws IOException;

	public abstract Object getAttachment();

	public abstract Object getAttachment(PluginContext context);

	public abstract Object getAttribute(Object key) ;
	
	public abstract ReentrantMap<Object, Object> getAttributes() ;

	public abstract NIOContext getContext();

	public abstract long getCreationTime();

	public abstract long getLastAccessTime();

	public abstract String getLocalAddr();

	public abstract String getLocalHost();

	public abstract int getLocalPort();

	public abstract InetSocketAddress getLocalSocketAddress();

	public abstract int getMaxIdleTime() throws SocketException;

	public abstract String getRemoteAddr();

	public abstract String getRemoteHost();

	public abstract int getRemotePort();

	public abstract InetSocketAddress getRemoteSocketAddress();

	public abstract Integer getSessionID();

	public abstract UDPEndPoint getUDPEndPoint();

	public abstract boolean isBlocking();

	public abstract boolean isOpened();

	public abstract void removeAttribute(Object key) ;
	
	public abstract void setAttachment(Object attachment);

	public abstract void setAttachment(PluginContext context, Object attachment);

	public abstract void setAttribute(Object key, Object value) ;

	public abstract void setSessionID(Integer sessionID);

	public abstract void setUDPEndPoint(UDPEndPoint udpEndPoint);

}