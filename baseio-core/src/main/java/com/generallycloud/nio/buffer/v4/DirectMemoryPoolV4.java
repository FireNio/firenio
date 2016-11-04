package com.generallycloud.nio.buffer.v4;

import java.nio.ByteBuffer;

public class DirectMemoryPoolV4 extends MemoryPoolV4{

	public DirectMemoryPoolV4(int capacity) {
		super(capacity);
	}
	
	public DirectMemoryPoolV4(int capacity,int unitMemorySize) {
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
