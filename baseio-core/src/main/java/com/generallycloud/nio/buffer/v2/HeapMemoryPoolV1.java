package com.generallycloud.nio.buffer.v2;

import java.nio.ByteBuffer;

import com.generallycloud.nio.buffer.v1.MemoryPoolV0;

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
