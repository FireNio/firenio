package com.generallycloud.nio.buffer.v5;

import java.nio.ByteBuffer;

public class HeapMemoryPoolV5 extends MemoryPoolV5{

	public HeapMemoryPoolV5(int capacity) {
		super(capacity);
	}
	
	public HeapMemoryPoolV5(int capacity,int unitMemorySize) {
		super(capacity,unitMemorySize);
	}

	protected ByteBuffer allocateMemory(int capacity) {
		return ByteBuffer.allocate(capacity);
	}

	public void freeMemory() {
		this.memory.clear();
	}
	
}
