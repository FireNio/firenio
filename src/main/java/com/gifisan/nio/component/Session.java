package com.gifisan.nio.component;

import java.net.SocketException;

import com.gifisan.nio.Attachment;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.server.NIOContext;

public interface Session extends Attributes{
	
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

	public abstract Attachment getAttachment(PluginContext context);

	public abstract NIOContext getContext();

	public abstract long getCreationTime();

	public abstract String getLocalAddr();

	public abstract String getLocalHost();

	public abstract int getLocalPort();

	public abstract String getMachineType() ;

	public abstract int getMaxIdleTime() throws SocketException;

	public abstract String getRemoteAddr();

	public abstract String getRemoteHost();

	public abstract int getRemotePort();

	public abstract Long getSessionID();
	
	public abstract UDPEndPoint getUDPEndPoint();
	
	public abstract boolean isBlocking();
	
	public abstract boolean isOpened();
	
	public abstract void setAttachment(PluginContext context, Attachment attachment);

	public abstract void setMachineType(String machineType);
	
	public abstract void setUDPEndPoint(UDPEndPoint udpEndPoint);

}