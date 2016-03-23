package com.gifisan.nio.client;

import java.io.IOException;

import com.gifisan.nio.component.OutputStream;

public interface ClientSesssion {

	public abstract long getTimeout();

	public abstract Response request(String serviceName, String content) throws IOException;

	public abstract Response request(String serviceName, String content, int available) throws IOException;

	public abstract void setTimeout(long timeout);
	
	public abstract OutputStream getOutputStream();

}