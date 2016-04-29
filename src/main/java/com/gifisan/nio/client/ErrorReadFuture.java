package com.gifisan.nio.client;

import java.io.IOException;
import java.io.InputStream;

import com.gifisan.nio.component.future.AbstractReadFuture;
import com.gifisan.nio.component.future.IOReadFuture;

public class ErrorReadFuture extends AbstractReadFuture implements IOReadFuture{

	public ErrorReadFuture(String serviceName,String text,ProtectedClientSession session, InputStream inputStream,Exception exception) {
		super(null, null, session, serviceName);
		this.text = text;
		this.inputStream = inputStream;
		this.exception = exception;
	}
	
	private Exception exception = null;

	public Exception getException() {
		return exception;
	}

	public boolean read() throws IOException {
		return false;
	}

}
