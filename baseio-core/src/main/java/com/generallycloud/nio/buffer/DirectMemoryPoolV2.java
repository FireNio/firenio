package com.generallycloud.nio.buffer;

import java.nio.ByteBuffer;

@Deprecated
public class DirectMemoryPoolV2 extends MemoryPoolV2{

	public DirectMemoryPoolV2(int capacity) {
		super(capacity);
	}
	
	public DirectMemoryPoolV2(int capacity,int unitMemorySize) {
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
