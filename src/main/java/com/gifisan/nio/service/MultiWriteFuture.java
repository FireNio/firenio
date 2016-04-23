package com.gifisan.nio.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.gifisan.nio.component.AbstractWriteFuture;
import com.gifisan.nio.component.IOExceptionHandle;
import com.gifisan.nio.server.session.Session;

public class MultiWriteFuture extends AbstractWriteFuture implements WriteFuture {

	public MultiWriteFuture(Session session,String serviceName, ByteBuffer textBuffer, byte []textCache ,
			InputStream inputStream,IOExceptionHandle handle) {
		super(handle,serviceName, textBuffer, textCache, session);
		this.inputStream = inputStream;
		this.streamBuffer = ByteBuffer.allocate(1024 * 1000);
		this.streamBuffer.position(streamBuffer.limit());
	}

	private int		writedLength	= 0;
	private int		dataLength	= 0;
	private ByteBuffer	streamBuffer	= null;

	public boolean write() throws IOException {
		ByteBuffer buffer = this.textBuffer;

		if (buffer.hasRemaining()) {
			attackNetwork(endPoint.write(buffer));
			if (buffer.hasRemaining()) {
				return false;
			}
		}

		if (writedLength < dataLength) {
			buffer = streamBuffer;
			if (!buffer.hasRemaining()) {
				fill(inputStream, buffer);
			}
			int length = endPoint.write(buffer);
			attackNetwork(length);
			writedLength += length;
		}

		return writedLength == dataLength;
	}

	private void fill(InputStream inputStream, ByteBuffer buffer) throws IOException {
		byte[] array = buffer.array();
		int pos = 0;
		for (; pos < array.length;) {
			int n = inputStream.read(array, pos, array.length - pos);
			if (n <= 0)
				break;
			pos += n;
		}
		buffer.position(pos);
		buffer.flip();
	}
	
}
