package com.generallycloud.nio.buffer;

public interface PooledByteBuf extends ByteBuf {

	public abstract int getBeginUnit();

	public abstract ByteBuf produce(int begin, int end, int newLimit);

}
