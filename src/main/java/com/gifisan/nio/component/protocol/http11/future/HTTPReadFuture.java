package com.gifisan.nio.component.protocol.http11.future;

import java.io.OutputStream;
import java.util.Map;

import com.gifisan.nio.component.protocol.future.ReadFuture;

public abstract interface HTTPReadFuture extends ReadFuture {

	public abstract Map<String, String> getHeaders();

	public abstract String getHost();

	public abstract int getContentLength();

	public abstract String getRequestURI();

	public abstract String getMethod();

	public abstract String getVersion();

	public abstract String getBoundary();

	public abstract String getContentType();

	public abstract Map<String, String> getRequestParams();

	public abstract String getRequestParam(String key);

	public abstract String getParamString();

	public abstract int getStatus();

	public abstract void setStatus(int status);

	public abstract boolean hasOutputStream();
	
	public abstract OutputStream getOutputStream() ;

	public abstract void setOutputStream(OutputStream outputStream);
}
