package com.generallycloud.nio.buffer.v5;

import java.nio.ByteBuffer;

public class DirectMemoryPoolV5 extends MemoryPoolV5{

	public DirectMemoryPoolV5(int capacity) {
		super(capacity);
	}
	
	public DirectMemoryPoolV5(int capacity,int unitMemorySize) {
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
