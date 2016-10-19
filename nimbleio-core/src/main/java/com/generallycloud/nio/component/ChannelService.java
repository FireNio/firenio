package com.generallycloud.nio.component;

import java.net.InetSocketAddress;


public interface ChannelService{

	public abstract NIOContext getContext() ;

	public abstract void setContext(NIOContext context);
	
	public abstract InetSocketAddress getServerSocketAddress();
	
	public abstract String getServiceDescription();
	
	public abstract boolean isActive();
}
