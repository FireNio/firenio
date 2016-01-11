package com.gifisan.mtp.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ClientInputStream extends InputStream {

	private int			avaiable	= 0;
	private int			BLOCK	= 0;
	private ByteBuffer		buffer	= null;
	private int			position	= 0;
	private ClientEndPoint	endPoint	= null;

	public ClientInputStream(ClientEndPoint endPoint, int avaiable) {
		this.endPoint = endPoint;
		this.avaiable = avaiable;
	}

	public int available() throws IOException {
		return this.avaiable;
	}

	public void close() throws IOException {

	}

	public boolean complete() {
		return avaiable == 0 || position >= avaiable;
	}

	public void mark(int readlimit) {
		// throw new IOException("not support");
	}

	public boolean markSupported() {
		// throw new IOException("not support");
		return false;
	}

	public int read() throws IOException {
		throw new IOException("not support");
	}

	public int read(byte[] bytes) throws IOException {

		if (complete()) {
			return -1;
		}

		int limit = bytes.length;

		if (position + limit > avaiable) {
			limit = avaiable - position;
		}

		if (limit == BLOCK) {
			buffer.clear();
		} else if (limit > BLOCK) {
			BLOCK = limit;
			buffer = ByteBuffer.allocate(BLOCK);
		} else {
			buffer.clear();
			buffer.limit(limit);
		}

		int length = endPoint.read(buffer);
		for (;length < limit;) {
			int __length = endPoint.read(buffer);
			length += __length;
		}

		this.position += length;
		buffer.flip();
		buffer.get(bytes, 0, length);
		return length;
	}

	public int read(byte[] b, int off, int len) throws IOException {
		throw new IOException("not support");
	}

	public void reset() throws IOException {
		throw new IOException("not support");
	}

	public long skip(long n) throws IOException {
		throw new IOException("not support");
	}

}
