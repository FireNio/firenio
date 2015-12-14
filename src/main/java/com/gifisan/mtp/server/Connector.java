package com.gifisan.mtp.server;

import java.io.Closeable;
import java.io.IOException;

import com.gifisan.mtp.LifeCycle;

public interface Connector extends LifeCycle ,Closeable{
	
	public abstract void open() throws IOException;
	
	public abstract int getPort();
	
	public abstract int getLocalPort();
	
	public abstract void setPort(int port);
	
	public abstract MTPServer getServer();
	
	public abstract void setServer(MTPServer server);
	
	public abstract String getHost();
	
	public abstract void setHost(String host);
	
	
}
