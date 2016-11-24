package com.generallycloud.nio.component;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.buffer.ByteBufAllocator;

//FIXME 扩展更多Channel
public interface Channel extends Closeable{
	
	public abstract void active();

	public abstract long getCreationTime();

	public abstract long getLastAccessTime();

	public abstract String getLocalAddr();

	public abstract String getLocalHost();

	public abstract int getLocalPort();

	public abstract boolean isOpened();

	public abstract String getRemoteAddr();

	public abstract String getRemoteHost();

	public abstract int getRemotePort();
	
	public abstract void physicalClose();

	public abstract BaseContext getContext();
	
	public abstract void setAttachment(Object attachment);

	public abstract Object getAttachment();
	
	public abstract InetSocketAddress getLocalSocketAddress() ;
	
	public abstract Session getSession();

	public abstract Integer getChannelID();
	
	public abstract InetSocketAddress getRemoteSocketAddress();

	public abstract int getMaxIdleTime() throws SocketException;
	
	public abstract ReentrantLock getChannelLock() ;
	
	public abstract ByteBufAllocator getByteBufAllocator();

	public abstract boolean isInSelectorLoop();
	
}
