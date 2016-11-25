package com.generallycloud.nio.component;

import java.net.InetSocketAddress;
import java.nio.channels.SelectableChannel;

public interface ChannelService {

	public abstract ChannelContext getContext() ;

	public abstract InetSocketAddress getServerSocketAddress();
	
	public abstract boolean isActive();
	
	public abstract SelectableChannel getSelectableChannel();
}
