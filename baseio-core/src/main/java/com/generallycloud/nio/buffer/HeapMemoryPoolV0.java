package com.generallycloud.nio.buffer;

import java.nio.ByteBuffer;

@Deprecated
public class HeapMemoryPoolV0 extends MemoryPoolV0{

	public HeapMemoryPoolV0(int capacity) {
		super(capacity);
	}

	protected ByteBuffer allocateMemory(int capacity) {
		return ByteBuffer.allocate(capacity);
	}

	public void freeMemory() {
		
	}
}
