package com.generallycloud.nio.component;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class ChannelBufferOutputstream extends OutputStream implements HeapOutputStream{

	private ByteBuffer	buffer;

	public int size() {
		return buffer.limit();
	}

	public byte[] toByteArray() {
		return array();
	}
	
	public byte[] array() {
		return buffer.array();
	}

	public void write(int b) throws IOException {
		throw new UnsupportedOperationException();
	}

	public ByteBuffer getBuffer() {
		return buffer;
	}

	public void setBuffer(ByteBuffer buffer) {
		this.buffer = buffer;
	}
}
