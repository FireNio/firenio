package com.gifisan.nio.service;

import java.io.InputStream;

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
