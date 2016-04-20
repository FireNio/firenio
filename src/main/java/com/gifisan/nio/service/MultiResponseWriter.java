package com.gifisan.nio.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.CatchWriteException;
import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.server.InnerRequest;

public class MultiResponseWriter extends AbstractResponseWriter implements ResponseWriter {

	private int			writedLength	= 0;
	private int			dataLength	= 0;
	private InputStream		inputStream	= null;
	private ByteBuffer		streamBuffer	= null;

	public MultiResponseWriter(ByteBuffer buffer, EndPoint endPoint, byte sessionID, InnerRequest request,
			CatchWriteException catchWriteException, int writedLength, int dataLength, InputStream inputStream) {
		super(buffer, endPoint, sessionID, request, catchWriteException);
		this.writedLength = writedLength;
		this.dataLength = dataLength;
		this.inputStream = inputStream;
		this.streamBuffer = ByteBuffer.allocate(1024 * 1000);
		this.streamBuffer.position(streamBuffer.limit());
	}

	public boolean complete() {
		if (!buffer.hasRemaining() && writedLength == dataLength) {
			CloseUtil.close(inputStream);
			return true;
		}
		return false;
	}

	public boolean doWrite() throws IOException {
		ByteBuffer buffer = this.buffer;
		
		if (buffer.hasRemaining()) {
			endPoint.write(buffer);
			if (buffer.hasRemaining()) {
				return false;
			}
		}

		if (writedLength < dataLength) {
			buffer = streamBuffer;
			if (buffer.hasRemaining()) {
				writedLength += endPoint.write(buffer);
			} else {
				fill(inputStream, buffer);
				writedLength += endPoint.write(buffer);
			}
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
