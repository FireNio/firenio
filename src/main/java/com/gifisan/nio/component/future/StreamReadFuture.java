package com.gifisan.nio.component.future;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.TCPEndPoint;

public class StreamReadFuture extends MultiReadFuture implements IOReadFuture {

	public StreamReadFuture(Session session, ByteBuffer header) {
		super(session, header);
	}

	protected int gainTextLength(byte[] header) {
		return 0;
	}

	protected void gainText(TCPEndPoint endPoint, ByteBuffer header, ByteBuffer buffer) throws IOException {
	}

}
