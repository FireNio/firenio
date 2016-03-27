package com.gifisan.nio.client;

import java.io.IOException;
import java.io.InputStream;

public interface ClientSesssion {

	public abstract long getTimeout();

	public abstract ClientResponse request(String serviceName, String content) throws IOException;

	public abstract ClientResponse request(String serviceName, String content, InputStream inputStream) throws IOException;

	public abstract void setTimeout(long timeout);

}