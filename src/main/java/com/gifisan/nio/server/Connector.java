package com.gifisan.nio.server;

import java.io.Closeable;

import com.gifisan.nio.component.Connectable;

public interface Connector extends Connectable ,Closeable{
	
	public abstract void setPort(int port);
	
	public abstract int getPort();
}
