package com.generallycloud.nio.buffer;

public interface PooledByteBuf extends ByteBuf {

	public abstract int getBeginUnit();

	public abstract PooledByteBuf produce(int begin, int end, int newLimit);
	
	public abstract PooledByteBuf produce(PooledByteBuf buf);

}
