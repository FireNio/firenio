package com.gifisan.nio.component.future;

import java.io.IOException;

import com.gifisan.nio.component.BufferedOutputStream;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.TCPEndPoint;

public interface IOReadFuture extends ReadFuture{

	public abstract boolean read() throws IOException;

	public abstract TCPEndPoint getEndPoint();
	
	public abstract Session getSession();
	
	public abstract void flush();
	
	public abstract BufferedOutputStream getTextCache();
	
}
