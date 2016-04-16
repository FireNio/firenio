package com.gifisan.nio.service;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.component.CatchWriteException;
import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.server.InnerRequest;

public class TextResponseWriter extends AbstractResponseWriter implements ResponseWriter {

	public TextResponseWriter(ByteBuffer buffer, EndPoint endPoint, byte sessionID, InnerRequest request,
			CatchWriteException catchWriteException) {
		super(buffer, endPoint, sessionID, request, catchWriteException);
	}

	public boolean complete() {
		return !this.buffer.hasRemaining();
	}

	public void doWrite() throws IOException {
		if (buffer.hasRemaining()) {
			endPoint.write(buffer);
		}
	}
}
