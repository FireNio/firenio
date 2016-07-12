package com.gifisan.nio.component;

import java.net.InetSocketAddress;
import java.net.SocketException;

import com.gifisan.nio.Attachment;
import com.gifisan.nio.component.concurrent.ReentrantMap;
import com.gifisan.nio.component.protocol.future.ReadFuture;
import com.gifisan.nio.extend.PluginContext;

public interface Session {

	public abstract boolean closed();

	public abstract void destroy();

	/**
	 * 该方法能主动关闭EndPoint，但是可能会因为线程同步导致MainSelector抛出
	 * </BR>java.nio.channels.ClosedChannelException
	 * 
	 * @see java.nio.channels.ClosedChannelException
	 */
	public abstract void disconnect();

	public abstract void flush(ReadFuture future);

	public abstract Attachment getAttachment();

	public abstract void setAttachment(Attachment attachment);

	public abstract Attachment getAttachment(PluginContext context);

	public abstract NIOContext getContext();

	public abstract long getCreationTime();

	public abstract String getLocalAddr();

	public abstract String getLocalHost();

	public abstract int getLocalPort();

	public abstract String getMachineType();

	public abstract int getMaxIdleTime() throws SocketException;

	public abstract String getRemoteAddr();

	public abstract String getRemoteHost();

	public abstract int getRemotePort();

	public abstract Integer getSessionID();

	public abstract UDPEndPoint getUDPEndPoint();

	public abstract boolean isBlocking();

	public abstract boolean isOpened();

	public abstract void setAttachment(PluginContext context, Attachment attachment);

	public abstract void setMachineType(String machineType);

	public abstract void setUDPEndPoint(UDPEndPoint udpEndPoint);

	public abstract void setSessionID(Integer sessionID);

	public abstract InetSocketAddress getLocalSocketAddress();

	public abstract InetSocketAddress getRemoteSocketAddress();
	
	public abstract void removeAttribute(Object key) ;

	public abstract void setAttribute(Object key, Object value) ;

	public abstract Object getAttribute(Object key) ;

	public abstract ReentrantMap<Object, Object> getAttributes() ;

	public abstract void clearAttributes() ;

}