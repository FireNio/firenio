package com.gifisan.nio.component.future;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.IOEventHandle;
import com.gifisan.nio.component.Session;

public class MultiWriteFuture extends AbstractWriteFuture implements WriteFuture {

	public MultiWriteFuture(EndPoint endPoint,Session session,String serviceName, ByteBuffer textBuffer, byte []textCache ,
			InputStream inputStream,IOEventHandle handle) throws IOException {
		super(endPoint,handle,serviceName, textBuffer, textCache, session);
		this.inputStream = inputStream;
		this.dataLength = inputStream.available();
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
