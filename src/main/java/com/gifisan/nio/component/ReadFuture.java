package com.gifisan.nio.component;

import java.io.OutputStream;


public interface ReadFuture {

	public abstract String getServiceName() ;
	
	public abstract String getText();
	
	public abstract Parameters getParameters();
	
	public abstract OutputStream getOutputStream();
	
	public abstract void setIOEvent(OutputStream outputStream,IOExceptionHandle handle);
	
	
}
