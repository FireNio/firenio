package com.generallycloud.nio.component.protocol;

import java.io.IOException;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.TCPEndPoint;

public class EmptyWriteFuture extends AbstractWriteFuture implements IOWriteFuture {
	
	private static final Logger logger = LoggerFactory.getLogger(EmptyWriteFuture.class);

	public EmptyWriteFuture(TCPEndPoint endPoint) {
		super(endPoint, null, null);
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
