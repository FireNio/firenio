package com.generallycloud.nio.component;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;

//FIXME 扩展更多Channel
public interface Channel extends Closeable{

	public abstract String getLocalAddr();

	public abstract String getLocalHost();

	public abstract int getLocalPort();

	public abstract boolean isOpened();

	public abstract String getRemoteAddr();

	public abstract String getRemoteHost();

	public abstract int getRemotePort();
	
	//FIXME 考虑放在IO线程执行否则会导致java.nio.channels.CancelledKeyException异常
	public abstract void physicalClose() throws IOException;

	public abstract NIOContext getContext();
	
	public abstract void setAttachment(Object attachment);

	public abstract Object getAttachment();
	
	public abstract InetSocketAddress getLocalSocketAddress() ;
	
	public abstract IOSession getSession();

	public abstract Integer getChannelID();
	
	public abstract InetSocketAddress getRemoteSocketAddress();

	public abstract int getMaxIdleTime() throws SocketException;
	
}
