package com.gifisan.nio.component.future;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.component.TCPEndPoint;

public class StreamReadFuture extends MultiReadFuture implements IOReadFuture {

	public StreamReadFuture(TCPEndPoint endPoint, ByteBuffer header) {
		super(endPoint, header);
	}

	protected int gainTextLength(byte[] header) {
		return 0;
	}

	protected void gainText(TCPEndPoint endPoint, ByteBuffer header, ByteBuffer buffer) throws IOException {
	}

}
