package com.gifisan.nio.component;

import java.io.IOException;
import java.nio.ByteBuffer;

public class TextWriteFuture extends AbstractWriteFuture implements WriteFuture {

	public TextWriteFuture(Session session, String serviceName, ByteBuffer textBuffer, byte[] textCache,
			IOExceptionHandle handle) {
		super(handle, serviceName, textBuffer, textCache, session);
	}

	public boolean write() throws IOException {
		ByteBuffer buffer = this.textBuffer;
		attackNetwork(endPoint.write(buffer));
		return !buffer.hasRemaining();
	}
}
