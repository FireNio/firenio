package com.gifisan.nio.component.future;

import java.io.InputStream;
import java.io.OutputStream;

import com.gifisan.nio.component.IOExceptionHandle;
import com.gifisan.nio.component.Parameters;

public interface ReadFuture extends Future{

	public abstract Parameters getParameters();
	
	public abstract OutputStream getOutputStream();
	
	public abstract InputStream getInputStream();
	
	public abstract void setOutputIOEvent(OutputStream outputStream,IOExceptionHandle handle);
	
	public abstract void setInputIOEvent(InputStream inputStream,IOExceptionHandle handle);
	
	public abstract boolean hasOutputStream();
	
	public abstract int getStreamLength();
	
}
