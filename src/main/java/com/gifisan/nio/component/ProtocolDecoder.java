package com.gifisan.nio.component;

import java.io.IOException;

import com.gifisan.nio.server.ServerEndPoint;

public interface ProtocolDecoder {

	public abstract InputStream getInputStream();

	public abstract String getContent();

	public abstract String getServiceName();
	
	public abstract byte getSessionID();

	public abstract boolean isBeat();

	public abstract boolean decode(ServerEndPoint endPoint) throws IOException;

}