package com.gifisan.nio.service;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.component.IOExceptionHandle;
import com.gifisan.nio.server.session.Session;

public class TextWriteFuture extends AbstractWriteFuture implements WriteFuture {

	public TextWriteFuture(IOExceptionHandle handle, ByteBuffer textBuffer, Session session) {
		super(handle, textBuffer, session);
	}

	public boolean write() throws IOException {
		ByteBuffer buffer = this.textBuffer;
		attackNetwork(endPoint.write(buffer));
		return !buffer.hasRemaining();
	}
}
