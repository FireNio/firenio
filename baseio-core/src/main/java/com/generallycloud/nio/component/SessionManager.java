package com.generallycloud.nio.component;

import java.io.Closeable;

public interface SessionManager extends Closeable{

	public abstract int getManagedSessionSize();
	
	public abstract void loop();

}