package com.generallycloud.nio.buffer;

public class EmptyByteBuf extends HeapByteBuf {

	private EmptyByteBuf() {
		super(UnpooledByteBufAllocator.getInstance(), new byte[] {});
	}

	public static EmptyByteBuf EMPTY_BYTEBUF = new EmptyByteBuf();

	@Override
	public void release() {
		
	}

	@Override
	public ByteBuf duplicate() {
		return this;
	}

}
