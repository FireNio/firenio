package com.generallycloud.nio.buffer;

import java.nio.ByteBuffer;

public class HeapMemoryPoolV3 extends MemoryPoolV3{

	public HeapMemoryPoolV3(int capacity) {
		super(capacity);
	}
	
	public HeapMemoryPoolV3(int capacity,int unitMemorySize) {
		super(capacity,unitMemorySize);
	}

	protected ByteBuffer allocateMemory(int capacity) {
		return ByteBuffer.allocate(capacity);
	}

	public void freeMemory() {
		this.memory.clear();
	}
	
}
