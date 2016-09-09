package com.generallycloud.nio.component.protocol.nio.future;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.TCPEndPoint;

public class StreamReadFuture extends MultiReadFuture {

	public StreamReadFuture(Session session, ByteBuf header) throws IOException {
		super(session, header);
	}

	protected int gainTextLength(byte[] header) {
		return 0;
	}

	protected void gainText(TCPEndPoint endPoint, ByteBuf buffer) throws IOException {
	}

}
