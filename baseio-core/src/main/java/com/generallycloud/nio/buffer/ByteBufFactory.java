package com.generallycloud.nio.buffer;

public interface ByteBufFactory extends ByteBufNew{

	public abstract void freeMemory();

	public abstract void initializeMemory(int capacity);

}
