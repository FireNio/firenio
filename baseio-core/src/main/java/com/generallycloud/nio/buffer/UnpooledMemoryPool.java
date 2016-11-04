package com.generallycloud.nio.buffer;

import java.nio.ByteBuffer;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.buffer.ByteBufferPool;
import com.generallycloud.nio.buffer.v5.MemoryBlockV5;

public class UnpooledMemoryPool {

	public static ByteBuf allocate(int capacity) {
		return new UnpooledMemoryBlock(ByteBuffer.allocate(capacity));
	}
	
	public static ByteBuf wrap(ByteBuffer buffer) {
		return new UnpooledMemoryBlock(buffer);
	}

	public static ByteBuf allocateDirect(int capacity) {
		return new UnpooledMemoryBlock(ByteBuffer.allocateDirect(capacity));
	}

	static class UnpooledMemoryBlock extends MemoryBlockV5 {

		private UnpooledMemoryBlock(ByteBuffer memory) {
			super(memory);
		}

		private UnpooledMemoryBlock(ByteBufferPool byteBufferPool, ByteBuffer memory) {
			super(byteBufferPool, memory);
		}

		public void release() {

		}

		public ByteBuf duplicate() {
			return new UnpooledMemoryBlock(nioBuffer().duplicate());
		}
	}

}
