package com.gifisan.nio.server;

import java.io.IOException;

import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.IOWriteFuture;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.AbstractWriteFuture;

public class EmptyReadFuture extends AbstractWriteFuture implements IOWriteFuture{

	public EmptyReadFuture(EndPoint endPoint,Session session) {
		super(endPoint, null, null, null, null, session);
	}

	public boolean write() throws IOException {
		return false;
	}
}
