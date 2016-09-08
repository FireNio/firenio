package com.generallycloud.nio.buffer;

public interface PooledByteBuf extends ByteBuf {

	public MemoryUnit getStart();

	public int getSize();

	public MemoryUnit getEnd();

	public PooledByteBuf getPrevious();

	public void setPrevious(PooledByteBuf previous);

	public PooledByteBuf getNext();

	public void setNext(PooledByteBuf next);

	public PooledByteBuf use();

	public PooledByteBuf free();

	public boolean using();
	
	public abstract void setMemory(MemoryUnit start, MemoryUnit end);

}
