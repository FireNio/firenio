package com.generallycloud.nio.connector;

import java.io.Closeable;

import com.generallycloud.nio.component.Connectable;
import com.generallycloud.nio.component.ChannelService;
import com.generallycloud.nio.component.Session;

public interface ChannelConnector extends ChannelService, Connectable, Closeable {

	public abstract Session getSession();
	
	public abstract boolean isConnected();
	
	public abstract long getTimeout() ;

	public abstract void setTimeout(long timeout) ;
}
