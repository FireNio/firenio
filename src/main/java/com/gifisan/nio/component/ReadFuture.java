package com.gifisan.nio.component;

import java.io.OutputStream;

public interface ReadFuture extends Future{

	public abstract Parameters getParameters();
	
	public abstract OutputStream getOutputStream();
	
	public abstract void setIOEvent(OutputStream outputStream,IOExceptionHandle handle);
	
	public abstract boolean hasOutputStream();
	
}
