package com.generallycloud.nio.buffer;

import java.nio.ByteBuffer;

@Deprecated
public class HeapMemoryPoolV1 extends MemoryPoolV0{

	public HeapMemoryPoolV1(int capacity) {
		super(capacity);
	}

	protected ByteBuffer allocateMemory(int capacity) {
		return ByteBuffer.allocate(capacity);
	}

	public void freeMemory() {
	}
}
