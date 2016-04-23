package com.gifisan.nio.component;

import java.io.OutputStream;

import com.gifisan.nio.service.Future;

public interface ReadFuture extends Future{

	public abstract Parameters getParameters();
	
	public abstract OutputStream getOutputStream();
	
	public abstract void setIOEvent(OutputStream outputStream,IOExceptionHandle handle);
	
	public abstract boolean hasOutputStream();
	
}
