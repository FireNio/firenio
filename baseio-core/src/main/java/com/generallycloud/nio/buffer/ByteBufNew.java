package com.generallycloud.nio.buffer;

public interface ByteBufNew {

	public abstract PooledByteBuf newByteBuf(ByteBufAllocator allocator);
}
