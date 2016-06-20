package com.gifisan.nio.component.future;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.component.ByteArrayInputStream;
import com.gifisan.nio.component.IOEventHandle;
import com.gifisan.nio.component.TCPEndPoint;

//ByteArrayInputStreamResponseWriter
public class ByteArrayWriteFuture extends AbstractWriteFuture {

	public ByteArrayWriteFuture(TCPEndPoint endPoint, Integer futureID, String serviceName, ByteBuffer textBuffer,
			byte[] textCache, ByteArrayInputStream inputStream, IOEventHandle handle) {
		super(endPoint, handle, futureID, serviceName, textBuffer, textCache);
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
