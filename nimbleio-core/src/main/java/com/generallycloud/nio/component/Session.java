package com.generallycloud.nio.component;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Map;

import com.generallycloud.nio.component.concurrent.EventLoop;
import com.generallycloud.nio.component.protocol.ProtocolEncoder;
import com.generallycloud.nio.component.protocol.ReadFuture;

public interface Session extends Closeable{

	public abstract void active();
	
	public abstract void clearAttributes() ;

	public abstract boolean isClosed();

	public abstract void flush(ReadFuture future) throws IOException;

	public abstract Object getAttachment();

	public abstract Object getAttachment(int index);

	public abstract Object getAttribute(Object key) ;
	
	public abstract Map<Object, Object> getAttributes() ;

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

	public abstract DatagramChannel getDatagramChannel();

	public abstract boolean isBlocking();

	public abstract Object removeAttribute(Object key) ;
	
	public abstract void setAttachment(Object attachment);

	public abstract void setAttachment(int index, Object attachment);

	public abstract void setAttribute(Object key, Object value) ;

	public abstract void setSessionID(Integer sessionID);

	public abstract void setDatagramChannel(DatagramChannel datagramChannel);
	
	public abstract EventLoop getEventLoop();
	
	public abstract String getProtocolID();
	
	public abstract ProtocolEncoder getProtocolEncoder();

	public abstract boolean isOpened();

}