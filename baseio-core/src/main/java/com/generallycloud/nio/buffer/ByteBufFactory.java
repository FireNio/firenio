package com.generallycloud.nio.buffer;

public interface ByteBufFactory {

	public abstract PooledByteBuf newByteBuf(ByteBufAllocator allocator);

	public abstract void freeMemory();

	public abstract void initializeMemory(int capacity);

}
