package com.gifisan.nio.service;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.component.AbstractWriteFuture;
import com.gifisan.nio.component.ByteArrayInputStream;
import com.gifisan.nio.component.IOExceptionHandle;
import com.gifisan.nio.server.session.Session;

//ByteArrayInputStreamResponseWriter
public class ByteArrayWriteFuture extends AbstractWriteFuture {

	public ByteArrayWriteFuture(Session session,String serviceName, ByteBuffer textBuffer, byte []textCache ,
			ByteArrayInputStream inputStream,IOExceptionHandle handle) {
		super(handle,serviceName, textBuffer, textCache, session);
		this.inputStream = inputStream;
		this.streamBuffer = ByteBuffer.wrap(inputStream.toByteArray());
	}

	private ByteBuffer	streamBuffer	= null;

	public boolean write() throws IOException {
		ByteBuffer buffer = this.textBuffer;

		if (buffer.hasRemaining()) {
			attackNetwork(endPoint.write(buffer));
			if (buffer.hasRemaining()) {
				return false;
			}
		}

		buffer = streamBuffer;

		if (buffer.hasRemaining()) {
			attackNetwork(endPoint.write(buffer));

			return !buffer.hasRemaining();
		}

		return true;
	}

}
