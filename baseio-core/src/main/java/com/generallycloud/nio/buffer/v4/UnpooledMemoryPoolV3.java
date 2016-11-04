package com.generallycloud.nio.buffer.v4;

import java.nio.ByteBuffer;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.buffer.ByteBufferPool;

public class UnpooledMemoryPoolV3 {

	public static ByteBuf allocate(int capacity) {
		return new UnpooledMemoryBlockV3(ByteBuffer.allocate(capacity));
	}
	
	public static ByteBuf wrap(ByteBuffer buffer) {
		return new UnpooledMemoryBlockV3(buffer);
	}

	public static ByteBuf allocateDirect(int capacity) {
		return new UnpooledMemoryBlockV3(ByteBuffer.allocateDirect(capacity));
	}

	static class UnpooledMemoryBlockV3 extends MemoryBlockV3 {

		private UnpooledMemoryBlockV3(ByteBuffer memory) {
			super(memory);
		}

		private UnpooledMemoryBlockV3(ByteBufferPool byteBufferPool, ByteBuffer memory) {
			super(byteBufferPool, memory);
		}

		public void release() {

		}

		public ByteBuf duplicate() {
			return new UnpooledMemoryBlockV3(getMemory().duplicate());
		}
	}

}
