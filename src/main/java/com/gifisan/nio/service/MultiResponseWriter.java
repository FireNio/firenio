package com.gifisan.nio.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.CatchWriteException;
import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.server.InnerRequest;

public class MultiResponseWriter extends AbstractResponseWriter implements ResponseWriter {

	private int		writedLength	= 0;
	private int		dataLength	= 0;
	private InputStream	inputStream	= null;
	private ByteBuffer	streamBuffer	= null;

	public MultiResponseWriter(ByteBuffer buffer, EndPoint endPoint, byte sessionID, InnerRequest request,
			CatchWriteException catchWriteException, int writedLength, int dataLength, InputStream inputStream,
			ByteBuffer streamBuffer) {
		super(buffer, endPoint, sessionID, request, catchWriteException);
		this.writedLength = writedLength;
		this.dataLength = dataLength;
		this.inputStream = inputStream;
		this.streamBuffer = streamBuffer;
	}

	public boolean complete() {
		if (!buffer.hasRemaining() && writedLength == dataLength) {
			CloseUtil.close(inputStream);
			return true;
		}
		return false;
	}

	public void doWrite() throws IOException {
		if (buffer.hasRemaining()) {
			endPoint.write(buffer);
			if (buffer.hasRemaining()) {
				return;
			}
		}

		if (writedLength < dataLength) {
			buffer = streamBuffer;
			if (buffer.hasRemaining()) {
				int length = endPoint.write(buffer);
				writedLength += length;
			} else {
				fill(inputStream, buffer);
				int length = endPoint.write(buffer);
				writedLength += length;
			}
		}
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
		buffer.limit(pos);
		buffer.flip();
	}
}
