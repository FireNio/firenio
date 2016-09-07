package com.generallycloud.nio.buffer;

import java.nio.ByteBuffer;

public class HeapByteBufferPool extends AbstractByteBufferPool{

	public HeapByteBufferPool(int capacity) {
		super(capacity);
	}

	protected ByteBuffer allocate(int capacity) {
		return ByteBuffer.allocate(capacity);
	}
}
