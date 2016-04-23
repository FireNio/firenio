package com.gifisan.nio.client;

import java.io.IOException;
import java.io.InputStream;

import com.gifisan.nio.component.ReadFuture;
import com.gifisan.nio.server.session.Session;

public interface ClientSession extends Session {

	public abstract ReadFuture request(String serviceName, String content) throws IOException;

	public abstract ReadFuture request(String serviceName, String content, InputStream inputStream) throws IOException;

	public abstract void write(String serviceName, String content, OnReadFuture onReadFuture) throws IOException;

	public abstract void write(String serviceName, String content, InputStream inputStream, OnReadFuture onReadFuture)
			throws IOException;

	public abstract ReadFuture poll(long timeout);

	public abstract long getTimeout();

	public abstract void setTimeout(long timeout);
	
	public abstract ClientContext getContext();

}