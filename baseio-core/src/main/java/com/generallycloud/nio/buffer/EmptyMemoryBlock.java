package com.generallycloud.nio.buffer;


public class EmptyMemoryBlock extends HeapByteBuf {

	private EmptyMemoryBlock() {
		super(new byte[] {});
	}

	public static EmptyMemoryBlock	EMPTY_BYTEBUF	= new EmptyMemoryBlock();

	public void release() {

	}

	public ByteBuf duplicate() {
		return this;
	}

}
