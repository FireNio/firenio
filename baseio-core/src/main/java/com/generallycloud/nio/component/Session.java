package com.generallycloud.nio.component;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.Map;

import com.generallycloud.nio.buffer.ByteBufAllocator;
import com.generallycloud.nio.component.concurrent.EventLoop;
import com.generallycloud.nio.protocol.ChannelWriteFuture;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ReadFuture;

public interface Session extends Closeable{

	public abstract void active();
	
	public abstract void clearAttributes() ;

	public abstract boolean isClosed();

	public abstract void flush(ReadFuture future) ;
	
	public abstract void flush(ChannelWriteFuture future);

	public abstract Object getAttachment();

	public abstract Object getAttachment(int index);

	public abstract Object getAttribute(Object key) ;
	
	public abstract Map<Object, Object> getAttributes() ;

	public abstract BaseContext getContext();

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

	public abstract boolean isBlocking();

	public abstract Object removeAttribute(Object key) ;
	
	public abstract void setAttachment(Object attachment);

	public abstract void setAttachment(int index, Object attachment);

	public abstract void setAttribute(Object key, Object value) ;

	public abstract void setSessionID(Integer sessionID);

	public abstract EventLoop getEventLoop();
	
	public abstract Charset getEncoding();
	
	public abstract String getProtocolID();
	
	public abstract ProtocolEncoder getProtocolEncoder();

	public abstract boolean isOpened();

	public abstract ByteBufAllocator getByteBufAllocator();

}