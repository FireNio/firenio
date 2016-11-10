package com.generallycloud.nio.buffer;


public class EmptyByteBuf extends HeapByteBuf {

	private EmptyByteBuf() {
		super(new byte[] {});
	}

	public static EmptyByteBuf	EMPTY_BYTEBUF	= new EmptyByteBuf();

	public void release() {

	}

	public ByteBuf duplicate() {
		return this;
	}

}
