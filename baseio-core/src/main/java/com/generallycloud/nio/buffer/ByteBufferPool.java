package com.generallycloud.nio.buffer;

import com.generallycloud.nio.LifeCycle;

public interface ByteBufferPool extends LifeCycle{

	public abstract void release(ByteBuf buffer);

	public abstract ByteBuf allocate(int capacity);
	
	public abstract int getUnitMemorySize();
	
	public abstract void freeMemory();
	
	public abstract int getCapacity();

}