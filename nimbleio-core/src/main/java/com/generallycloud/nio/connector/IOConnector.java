package com.generallycloud.nio.connector;

import java.io.Closeable;

import com.generallycloud.nio.component.Connectable;
import com.generallycloud.nio.component.IOService;
import com.generallycloud.nio.component.Session;

public interface IOConnector extends IOService, Connectable, Closeable {

	public abstract Session getSession();
	
	public abstract boolean isConnected();
	
	public abstract long getTimeout() ;

	public abstract void setTimeout(long timeout) ;
}
