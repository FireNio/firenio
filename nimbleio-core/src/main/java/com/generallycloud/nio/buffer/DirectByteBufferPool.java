package com.generallycloud.nio.buffer;

import java.nio.ByteBuffer;

public class DirectByteBufferPool extends AbstractByteBufferPool{

	public DirectByteBufferPool(int capacity) {
		super(capacity);
	}

	protected ByteBuffer allocate(int capacity) {
		return ByteBuffer.allocateDirect(capacity);
	}
}
