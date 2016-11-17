package com.generallycloud.nio.buffer;

public abstract class AbstractPooledByteBuf implements PooledByteBuf {

	protected boolean released = false;
	
	protected int beginUnit;

	public int getBeginUnit() {
		return beginUnit;
	}
	
}
