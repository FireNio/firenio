package com.gifisan.nio.server;

import java.io.IOException;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.future.AbstractWriteFuture;
import com.gifisan.nio.component.future.IOWriteFuture;

public class EmptyReadFuture extends AbstractWriteFuture implements IOWriteFuture{

	public EmptyReadFuture(TCPEndPoint endPoint,Session session) {
		super(endPoint, null, null, null, null, session);
	}

	public boolean write() throws IOException {
		return false;
	}
}
