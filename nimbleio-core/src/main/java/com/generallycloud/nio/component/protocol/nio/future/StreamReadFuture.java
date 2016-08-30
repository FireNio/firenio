package com.generallycloud.nio.component.protocol.nio.future;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.TCPEndPoint;

public class StreamReadFuture extends MultiReadFuture {

	public StreamReadFuture(Session session, ByteBuffer header) {
		super(session, header);
	}

	protected int gainTextLength(byte[] header) {
		return 0;
	}

	protected void gainText(TCPEndPoint endPoint, ByteBuffer header, ByteBuffer buffer) throws IOException {
	}

}
