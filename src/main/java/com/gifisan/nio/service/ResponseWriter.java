package com.gifisan.nio.service;

import java.io.IOException;

import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.server.InnerRequest;

public interface ResponseWriter {

	public abstract boolean complete();

	public abstract void doWrite() throws IOException;

	public abstract EndPoint getEndPoint();

	public abstract byte getSessionID();

	public abstract InnerRequest getRequest();

	public abstract void catchException(Request request, IOException e);
}
