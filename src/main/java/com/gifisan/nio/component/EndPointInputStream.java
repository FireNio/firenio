package com.gifisan.nio.component;

import java.io.IOException;
import java.nio.ByteBuffer;

public class EndPointInputStream implements InputStream {

	private int		avaiable	= 0;
	private EndPoint	endPoint	= null;
	private int		position	= 0;

	public EndPointInputStream(EndPoint endPoint, int avaiable) {
		this.endPoint = endPoint;
		this.avaiable = avaiable;
	}

	public int available() throws IOException {
		return this.avaiable;
	}

	public boolean complete() {
		return avaiable == 0 || position >= avaiable;
	}

	public void completedRead(ByteBuffer buffer) throws IOException {
		if (complete()) {
			buffer.limit(0);
			return;
		}

		int limit = buffer.capacity();

		if (position + limit > avaiable) {

			limit = avaiable - position;

			buffer.limit(limit);
		}

		endPoint.completedRead(buffer);

		this.position += limit;

	}

	public int read(ByteBuffer buffer) throws IOException {
		if (complete()) {
			return 0;
		}

		int limit = buffer.capacity();

		if (position + limit > avaiable) {

			limit = avaiable - position;

			buffer.limit(limit);
		}

		int _length = endPoint.read(buffer);

		this.position += _length;

		return _length;
	}

}
