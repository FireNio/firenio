package com.generallycloud.nio.buffer;

import java.nio.ByteBuffer;

public class DirectByteBufFactory implements ByteBufFactory {

	private ByteBuffer	memory	= null;

	@Override
	public void initializeMemory(int capacity) {
		this.memory = ByteBuffer.allocateDirect(capacity);
	}

	@Override
	public PooledByteBuf newByteBuf(ByteBufAllocator allocator) {
		return new DirectByteBuf(allocator, memory);
	}

	@Override
	@SuppressWarnings("restriction")
	public void freeMemory() {
		if (((sun.nio.ch.DirectBuffer) memory).cleaner() != null) {
			((sun.nio.ch.DirectBuffer) memory).cleaner().clean();
		}
	}

}
