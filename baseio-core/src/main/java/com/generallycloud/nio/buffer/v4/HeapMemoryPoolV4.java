package com.generallycloud.nio.buffer.v4;

import java.nio.ByteBuffer;

public class HeapMemoryPoolV4 extends MemoryPoolV4{

	public HeapMemoryPoolV4(int capacity) {
		super(capacity);
	}
	
	public HeapMemoryPoolV4(int capacity,int unitMemorySize) {
		super(capacity,unitMemorySize);
	}

	protected ByteBuffer allocateMemory(int capacity) {
		return ByteBuffer.allocate(capacity);
	}

	public void freeMemory() {
		this.memory.clear();
	}
	
}
