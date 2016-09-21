package com.generallycloud.nio.buffer;

import java.nio.ByteBuffer;

@Deprecated
public class DirectMemoryPoolV1 extends MemoryPoolV1{

	public DirectMemoryPoolV1(int capacity) {
		super(capacity);
	}

	protected ByteBuffer allocateMemory(int capacity) {
		return ByteBuffer.allocateDirect(capacity);
	}

	public void freeMemory() {
		
//		sun.nio.ch.DirectBuffer buffer = (sun.nio.ch.DirectBuffer) memory;
//		
//		buffer.cleaner().clean();
	}

}
