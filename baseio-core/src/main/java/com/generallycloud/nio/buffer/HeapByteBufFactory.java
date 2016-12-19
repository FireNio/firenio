package com.generallycloud.nio.buffer;

public class HeapByteBufFactory implements ByteBufFactory {

	private byte[]	memory	= null;

	@Override
	public void freeMemory() {
		this.memory = null;
	}

	@Override
	public void initializeMemory(int capacity) {
		this.memory = new byte[capacity];
	}

	@Override
	public PooledByteBuf newByteBuf(ByteBufAllocator allocator) {
		return new HeapByteBuf(allocator, memory);
	}

}
