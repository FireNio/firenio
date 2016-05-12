package com.gifisan.nio.component.future;

import java.io.IOException;

import com.gifisan.nio.component.BufferedOutputStream;
import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.IOEventHandle;
import com.gifisan.nio.component.Session;

public interface IOReadFuture extends ServerReadFuture{

	public abstract boolean read() throws IOException;

	public abstract TCPEndPoint getEndPoint();
	
	public abstract Session getSession();

	public abstract void catchOutputException(IOException e);
	
	public abstract void catchInputException(IOException e);
	
	public abstract boolean flushed();
	
	public abstract void flush();
	
	public abstract IOEventHandle getInputIOHandle();
	
	public abstract IOEventHandle getOutputIOHandle();
	
	public abstract BufferedOutputStream getTextCache();
	
}
