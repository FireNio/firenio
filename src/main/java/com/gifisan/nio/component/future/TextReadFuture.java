package com.gifisan.nio.component.future;

import java.io.IOException;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.TCPEndPoint;

public class TextReadFuture extends AbstractReadFuture implements IOReadFuture {

	public TextReadFuture(TCPEndPoint endPoint, Session session, String serviceName) {
		super(endPoint, session, serviceName);
	}

	public TextReadFuture(TCPEndPoint endPoint, Session session) {
		super(endPoint, session);
	}

	protected boolean doRead(TCPEndPoint endPoint) throws IOException {
		return true;
	}

}
