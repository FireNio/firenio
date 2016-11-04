package com.generallycloud.nio.buffer.v1;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.buffer.v2.MemoryUnitV1;

@Deprecated
public interface PooledByteBuf extends ByteBuf {

	public MemoryUnitV1 getStart();

	public int getSize();

	public MemoryUnitV1 getEnd();

	public PooledByteBuf getPrevious();

	public void setPrevious(PooledByteBuf previous);

	public PooledByteBuf getNext();

	public void setNext(PooledByteBuf next);

	public PooledByteBuf use();

	public PooledByteBuf free();

	public boolean using();
	
	public abstract void setMemory(MemoryUnitV1 start, MemoryUnitV1 end);

}
