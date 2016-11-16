package com.generallycloud.nio.buffer;

public class HeapByteBufFactory implements ByteBufFactory {

	private byte[]	memory	= null;

	public void freeMemory() {
		this.memory = null;
	}

	public void initializeMemory(int capacity) {
		this.memory = new byte[capacity];
	}

	public AbstractByteBuf newByteBuf(ByteBufAllocator allocator) {
		return new HeapByteBuf(allocator, memory);
	}

}
