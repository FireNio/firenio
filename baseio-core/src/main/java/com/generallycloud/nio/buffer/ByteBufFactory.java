package com.generallycloud.nio.buffer;

public interface ByteBufFactory {

	public abstract AbstractByteBuf newByteBuf(ByteBufAllocator allocator);

	public abstract void freeMemory();

	public abstract void initializeMemory(int capacity);

}
