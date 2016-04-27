package com.gifisan.nio.client;

import java.io.InputStream;

import com.gifisan.nio.component.future.ReadFutureImpl;

public class ErrorReadFuture extends ReadFutureImpl{

	public ErrorReadFuture(String serviceName,String text,InputStream inputStream,Exception exception) {
		super(serviceName);
		this.text = text;
		this.inputStream = inputStream;
		this.exception = exception;
	}
	
	private InputStream inputStream = null;
	
	private Exception exception = null;

	public InputStream getInputStream() {
		return inputStream;
	}

	public Exception getException() {
		return exception;
	}

}
