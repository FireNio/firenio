package com.generallycloud.nio.buffer;

public interface PooledByteBuf extends ByteBuf {

	public abstract int getIndex();

	public abstract void setIndex(int index);

	public abstract boolean isFree();

	public abstract void setFree(boolean free);

	public abstract int getBlockEnd();

	public abstract void setBlockEnd(int blockEnd);

	public abstract ByteBuf produce(int newLimit);

	public abstract int getBlockBegin();

	public abstract void setBlockBegin(int blockBegin);

}
