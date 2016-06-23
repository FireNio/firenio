package com.gifisan.nio.component.future;

import java.io.IOException;

import com.gifisan.nio.component.TCPEndPoint;

public class EmptyReadFuture extends AbstractWriteFuture implements IOWriteFuture {

	public EmptyReadFuture(TCPEndPoint endPoint) {
		super(endPoint, 0, null, null, null);
	}

	public boolean write() throws IOException {
		return false;
	}
}
