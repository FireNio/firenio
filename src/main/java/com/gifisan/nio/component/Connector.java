package com.gifisan.nio.component;

import java.io.Closeable;

public interface Connector extends Connectable ,Closeable{
	
	public abstract int getServerPort();
	
}
