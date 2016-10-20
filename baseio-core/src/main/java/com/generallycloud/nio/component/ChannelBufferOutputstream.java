package com.generallycloud.nio.component;

import java.io.IOException;
import java.io.OutputStream;

import com.generallycloud.nio.buffer.ByteBuf;

public class ChannelBufferOutputstream extends OutputStream implements HeapOutputStream {

	private ByteBuf	buffer;

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

	public ByteBuf getBuffer() {
		return buffer;
	}

	public void setBuffer(ByteBuf buffer) {
		this.buffer = buffer;
	}
}
