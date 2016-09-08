package com.generallycloud.nio.buffer;

import java.nio.ByteBuffer;

public class DirectMemoryPool extends MemoryPool{

	public DirectMemoryPool(int capacity) {
		super(capacity);
	}

	protected ByteBuffer allocate(int capacity) {
		return ByteBuffer.allocateDirect(capacity);
	}

	protected void freeMemory() {
		
		sun.nio.ch.DirectBuffer buffer = (sun.nio.ch.DirectBuffer) memory;
		
		buffer.cleaner().clean();
	}
	
	
}
