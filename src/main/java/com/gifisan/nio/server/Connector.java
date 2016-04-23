package com.gifisan.nio.server;

import java.io.Closeable;

import com.gifisan.nio.LifeCycle;
import com.gifisan.nio.client.Connectable;

public interface Connector extends Connectable, LifeCycle ,Closeable{
	
	public abstract void setPort(int port);
	
	public abstract int getPort();
}
