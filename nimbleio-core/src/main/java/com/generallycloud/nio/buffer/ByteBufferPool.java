package com.generallycloud.nio.buffer;

import com.generallycloud.nio.LifeCycle;

public interface ByteBufferPool extends LifeCycle{

	public abstract void release(ByteBuf buffer);

	public abstract ByteBuf poll(int capacity);
	
	public abstract int getUnitMemorySize();
	
	

}