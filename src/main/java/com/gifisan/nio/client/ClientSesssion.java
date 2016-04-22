package com.gifisan.nio.client;

import java.io.IOException;
import java.io.InputStream;

import com.gifisan.nio.component.ReadFuture;
import com.gifisan.nio.server.session.Session;

public interface ClientSesssion extends Session {

	public abstract void request(String serviceName, String content) throws IOException;

	public abstract void request(String serviceName, String content, InputStream inputStream) throws IOException;

	public abstract void request(String serviceName, String content, OnReadFuture onReadFuture) throws IOException;

	public abstract void request(String serviceName, String content, InputStream inputStream, OnReadFuture onReadFuture)
			throws IOException;

	public abstract ReadFuture poll(long timeout);
	
}