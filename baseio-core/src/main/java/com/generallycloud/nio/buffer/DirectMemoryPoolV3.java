package com.generallycloud.nio.buffer;

import java.nio.ByteBuffer;

public class DirectMemoryPoolV3 extends MemoryPoolV3{

	public DirectMemoryPoolV3(int capacity) {
		super(capacity);
	}
	
	public DirectMemoryPoolV3(int capacity,int unitMemorySize) {
		super(capacity,unitMemorySize);
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
