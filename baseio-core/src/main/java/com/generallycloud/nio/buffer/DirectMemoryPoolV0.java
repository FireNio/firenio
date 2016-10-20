package com.generallycloud.nio.buffer;

import java.nio.ByteBuffer;

@Deprecated
public class DirectMemoryPoolV0 extends MemoryPoolV0{

	public DirectMemoryPoolV0(int capacity) {
		super(capacity);
	}

	protected ByteBuffer allocateMemory(int capacity) {
		return ByteBuffer.allocateDirect(capacity);
	}

	public void freeMemory() {
		
	}
}
