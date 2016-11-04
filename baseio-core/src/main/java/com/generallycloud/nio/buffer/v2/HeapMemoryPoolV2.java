package com.generallycloud.nio.buffer.v2;

import java.nio.ByteBuffer;

import com.generallycloud.nio.buffer.v1.MemoryPoolV1;

@Deprecated
public class HeapMemoryPoolV2 extends MemoryPoolV1{

	public HeapMemoryPoolV2(int capacity) {
		super(capacity);
	}

	protected ByteBuffer allocateMemory(int capacity) {
		return ByteBuffer.allocate(capacity);
	}

	public void freeMemory() {
	}
}
