package com.gifisan.nio.component;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.net.SocketException;

import com.gifisan.nio.component.concurrent.ReentrantMap;
import com.gifisan.nio.component.protocol.future.ReadFuture;
import com.gifisan.nio.extend.PluginContext;

public interface Session extends Closeable{

	public abstract void active();
	
	public abstract void clearAttributes() ;

	public abstract boolean closed();

	/**
	 * 该方法为非线程安全，Connector端使用时应注意
	 */
	public abstract void close();

	/**
	 * 该方法能主动关闭EndPoint，但是可能会因为线程同步导致MainSelector抛出
	 * </BR>java.nio.channels.ClosedChannelException
	 * 
	 * @see java.nio.channels.ClosedChannelException
	 */
	public abstract void disconnect();

	public abstract void flush(ReadFuture future);

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