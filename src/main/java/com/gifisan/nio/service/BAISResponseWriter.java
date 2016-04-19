package com.gifisan.nio.service;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.component.ByteArrayInputStream;
import com.gifisan.nio.component.CatchWriteException;
import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.server.InnerRequest;

//ByteArrayInputStreamResponseWriter
public class BAISResponseWriter extends AbstractResponseWriter implements ResponseWriter {

	private ByteBuffer	streamBuffer	= null;

	public BAISResponseWriter(ByteBuffer buffer, EndPoint endPoint, byte sessionID, InnerRequest request,
			CatchWriteException catchWriteException, ByteArrayInputStream inputStream) {
		super(buffer, endPoint, sessionID, request, catchWriteException);
		this.streamBuffer = ByteBuffer.wrap(inputStream.toByteArray());
//		this.streamBuffer.flip();
	}

	public boolean complete() {
		return !buffer.hasRemaining() && !streamBuffer.hasRemaining();
	}

	public void doWrite() throws IOException {
		ByteBuffer buffer = this.buffer;

		if (buffer.hasRemaining()) {
			endPoint.write(buffer);
			if (buffer.hasRemaining()) {
				return;
			}
		}

		buffer = streamBuffer;

		if (buffer.hasRemaining()) {
			endPoint.write(buffer);
		}
	}

}
