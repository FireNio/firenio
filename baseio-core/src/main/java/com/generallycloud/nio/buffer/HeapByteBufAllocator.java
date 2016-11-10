package com.generallycloud.nio.buffer;


public class HeapByteBufAllocator extends AbstractByteBufAllocator {

	private byte[]		memory;

	public HeapByteBufAllocator(int capacity) {
		this(capacity, 1024);
	}

	public HeapByteBufAllocator(int capacity, int unitMemorySize) {
		super(capacity, unitMemorySize);
	}

	public void freeMemory() {
		this.memory = null;
	}

	protected void initializeMemory(int capacity) {
		this.memory = new byte[capacity];
	}

	protected AbstractByteBuf newByteBuf() {
		return new HeapByteBuf(this, memory);
	}

}
