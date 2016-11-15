package com.generallycloud.nio.buffer;

import java.nio.ByteBuffer;

public class DirectByteBufAllocator extends AbstractByteBufAllocator {

	private ByteBuffer	memory;

	protected void initializeMemory(int capacity) {
		this.memory = ByteBuffer.allocateDirect(capacity);
	}

	protected AbstractByteBuf newByteBuf() {
		return new DirectByteBuf(this, memory);
	}

	public DirectByteBufAllocator(int capacity) {
		this(capacity, 512);
	}

	public DirectByteBufAllocator(int capacity, int unitMemorySize) {
		super(capacity, unitMemorySize);
	}

	@SuppressWarnings("restriction")
	public void freeMemory() {
		if (((sun.nio.ch.DirectBuffer) memory).cleaner() != null) {
			((sun.nio.ch.DirectBuffer) memory).cleaner().clean();
		}
	}

}
