package com.generallycloud.nio.buffer;

import java.nio.ByteBuffer;

//FIXME throw
public class EmptyMemoryBlock extends SimulateByteBuf implements ByteBuf {

	public static EmptyMemoryBlock	EMPTY_BYTEBUF	= new EmptyMemoryBlock();

	private ByteBuffer				memory		= ByteBuffer.allocate(0);

	private EmptyMemoryBlock() {
	}

	public void release() {

	}

	public ByteBuffer nioBuffer() {
		return memory;
	}

}
