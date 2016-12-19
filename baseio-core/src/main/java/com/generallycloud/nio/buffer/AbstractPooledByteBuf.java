package com.generallycloud.nio.buffer;

public abstract class AbstractPooledByteBuf implements PooledByteBuf {

	protected boolean released = false;
	
	protected int beginUnit;

	@Override
	public int getBeginUnit() {
		return beginUnit;
	}

	@Override
	public PooledByteBuf newByteBuf(ByteBufAllocator allocator) {
		return this;
	}
	
}
