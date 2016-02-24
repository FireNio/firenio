package com.gifisan.nio.server;

import java.io.Closeable;
import java.io.IOException;

import com.gifisan.nio.LifeCycle;

public interface Connector extends LifeCycle ,Closeable{
	
	public abstract String getHost();
	
	public abstract int getPort();
	
	public abstract NIOServer getServer();
	
	public abstract void connect() throws IOException;
	
	public abstract void setHost(String host);
	
	public abstract void setPort(int port);
	
	
}
