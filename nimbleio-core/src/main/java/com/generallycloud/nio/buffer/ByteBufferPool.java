package com.generallycloud.nio.buffer;


public interface ByteBufferPool {

	public abstract void offer(ByteBuf buffer);

	public abstract ByteBuf poll(int capacity);

}