package com.gifisan.nio.component.future;

import java.io.IOException;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.TCPEndPoint;

public class EmptyWriteFuture extends AbstractWriteFuture implements IOWriteFuture {
	
	private static final Logger logger = LoggerFactory.getLogger(EmptyWriteFuture.class);

	public EmptyWriteFuture(TCPEndPoint endPoint) {
		super(endPoint, 0, null, null, null);
	}

	public boolean write() throws IOException {
		return true;
	}
	
	public void onException(IOException e) {
		logger.error(e.getMessage(),e);
	}

	public void onSuccess() {
	}
}
