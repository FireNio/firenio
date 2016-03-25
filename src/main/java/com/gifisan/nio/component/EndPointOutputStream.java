package com.gifisan.nio.component;

import java.io.IOException;
import java.nio.ByteBuffer;

public class EndPointOutputStream implements OutputStream {

	private EndPoint	endPoint	= null;
	
	public EndPointOutputStream(EndPoint endPoint) {
		this.endPoint = endPoint;
	}

	public int write(byte b) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(1);
		buffer.put(b);
		return endPoint.write(buffer);
	}

	public int write(byte[] bytes) throws IOException {
		return endPoint.write(ByteBuffer.wrap(bytes));
	}

	public int write(byte[] bytes, int offset, int length) throws IOException {
		return endPoint.write(ByteBuffer.wrap(bytes, offset, length));
	}

	public void completedWrite(ByteBuffer buffer) throws IOException {
		endPoint.completedWrite(buffer);
	}

	public void completedWrite(byte[] bytes, int offset, int length) throws IOException {
		endPoint.completedWrite(ByteBuffer.wrap(bytes, offset, length));
	}

	public void completedWrite(byte[] bytes) throws IOException {
		endPoint.completedWrite(ByteBuffer.wrap(bytes));

	}

}
