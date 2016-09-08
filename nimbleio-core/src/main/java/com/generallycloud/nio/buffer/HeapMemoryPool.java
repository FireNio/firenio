package com.generallycloud.nio.buffer;

import java.nio.ByteBuffer;

public class HeapMemoryPool extends MemoryPool{

	public HeapMemoryPool(int capacity) {
		super(capacity);
	}

	protected ByteBuffer allocate(int capacity) {
		return ByteBuffer.allocate(capacity);
	}

	protected void freeMemory() {
		this.memory.clear();
	}
	
}
