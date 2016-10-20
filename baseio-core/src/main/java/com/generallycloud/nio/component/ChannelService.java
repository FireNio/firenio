package com.generallycloud.nio.component;

import java.net.InetSocketAddress;


public interface ChannelService{

	public abstract BaseContext getContext() ;

	public abstract void setContext(BaseContext context);
	
	public abstract InetSocketAddress getServerSocketAddress();
	
	public abstract String getServiceDescription();
	
	public abstract boolean isActive();
}
