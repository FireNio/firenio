package com.gifisan.nio.component;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class MultiReadFuture extends AbstractReadFuture implements IOReadFuture {

	public MultiReadFuture(ByteBuffer textBuffer, Session session, String serviceName,int dataLength) {
		super(textBuffer, session, serviceName);
		this.hasStream = true;
		this.dataLength = dataLength;
		this.streamBuffer = ByteBuffer.allocate(1024 * 1000);
		this.streamBuffer.position(streamBuffer.limit());
	}

	private int				readLength	= 0;
	private int				dataLength	= 0;
	private ByteBuffer			streamBuffer	= null;

	public boolean read() throws IOException {
		ByteBuffer buffer = this.textBuffer;

		if (buffer.hasRemaining()) {
			endPoint.write(buffer);
			if (buffer.hasRemaining()) {
				return false;
			}
		}

		if (readLength < dataLength) {
			buffer = streamBuffer;

			endPoint.read(buffer);

			fill(outputStream, buffer);
		}

		return readLength == dataLength;
	}

	private void fill(OutputStream outputStream, ByteBuffer buffer) throws IOException {

		byte[] array = buffer.array();

		int length = buffer.position();

		if (length == 0) {
			return;
		}

		readLength += length;

		outputStream.write(array, 0, buffer.position());

		buffer.clear();
	}
}
