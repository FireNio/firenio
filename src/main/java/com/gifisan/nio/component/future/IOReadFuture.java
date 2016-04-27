package com.gifisan.nio.component.future;

import java.io.IOException;

import com.gifisan.nio.component.BufferedOutputStream;
import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.IOExceptionHandle;
import com.gifisan.nio.component.Session;

public interface IOReadFuture extends ServerReadFuture{

	public abstract boolean read() throws IOException;

	public abstract EndPoint getEndPoint();
	
	public abstract Session getSession();

	public abstract void catchOutputException(IOException e);
	
	public abstract void catchInputException(IOException e);
	
	public abstract boolean flushed();
	
	public abstract void flush();
	
	public abstract IOExceptionHandle getInputIOHandle();
	
	public abstract IOExceptionHandle getOutputIOHandle();
	
	public abstract BufferedOutputStream getTextCache();
	
}
