package com.gifisan.nio.component.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.component.ByteArrayInputStream;
import com.gifisan.nio.component.TCPEndPoint;

//ByteArrayInputStreamResponseWriter
public class ByteArrayWriteFuture extends AbstractWriteFuture {

	public ByteArrayWriteFuture(TCPEndPoint endPoint, ReadFuture readFuture, ByteBuffer textBuffer,
			ByteArrayInputStream inputStream) {
		super(endPoint, readFuture, textBuffer);
		this.inputStream = inputStream;
		this.streamBuffer = ByteBuffer.wrap(inputStream.toByteArray());
	}

	private ByteBuffer	streamBuffer;

	public boolean write() throws IOException {
		ByteBuffer buffer = this.textBuffer;

		if (buffer.hasRemaining()) {

			updateNetworkState(endPoint.write(buffer));

			if (buffer.hasRemaining()) {
				return false;
			}
		}

		buffer = streamBuffer;

		if (buffer.hasRemaining()) {

			updateNetworkState(endPoint.write(buffer));

			return !buffer.hasRemaining();
		}

		return true;
	}

}
