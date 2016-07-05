package com.gifisan.nio.component.future;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.gifisan.nio.component.TCPEndPoint;

public class MultiWriteFuture extends AbstractWriteFuture implements WriteFuture {

	public MultiWriteFuture(TCPEndPoint endPoint, Integer futureID, String serviceName, ByteBuffer textBuffer,
			byte[] textCache, InputStream inputStream) throws IOException {
		super(endPoint, futureID, serviceName, textBuffer, textCache);
		this.inputStream = inputStream;
		this.dataLength = inputStream.available();
		this.streamBuffer = ByteBuffer.allocate(1024 * 1000);
		this.streamBuffer.position(streamBuffer.limit());
	}

	private int		writedLength	;
	private int		dataLength	;
	private ByteBuffer	streamBuffer	;

	public boolean write() throws IOException {
		ByteBuffer buffer = this.textBuffer;

		if (buffer.hasRemaining()) {
			
			updateNetworkState(endPoint.write(buffer));
			
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
			
			updateNetworkState(length);
			
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
