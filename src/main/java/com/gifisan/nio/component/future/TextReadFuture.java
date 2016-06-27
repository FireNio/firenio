package com.gifisan.nio.component.future;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.TCPEndPoint;

public class TextReadFuture extends AbstractReadFuture implements IOReadFuture {

	public TextReadFuture(Session session, Integer futureID, String serviceName) {
		super(session, futureID, serviceName);
	}

	public TextReadFuture(Session session, ByteBuffer header) {
		super(session, header);
	}

	protected boolean doRead(TCPEndPoint endPoint) throws IOException {
		return true;
	}

}
