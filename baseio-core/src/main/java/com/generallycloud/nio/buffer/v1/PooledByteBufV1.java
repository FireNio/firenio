package com.generallycloud.nio.buffer.v1;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.buffer.v2.MemoryUnitV2;

@Deprecated
public interface PooledByteBufV1 extends ByteBuf {

	public MemoryUnitV2 getStart();

	public int getSize();

	public MemoryUnitV2 getEnd();

	public PooledByteBufV1 getPrevious();

	public void setPrevious(PooledByteBufV1 previous);

	public PooledByteBufV1 getNext();

	public void setNext(PooledByteBufV1 next);

	public PooledByteBufV1 use();

	public PooledByteBufV1 free();

	public boolean using();
	
	public abstract void setMemory(MemoryUnitV2 start, MemoryUnitV2 end);

}
