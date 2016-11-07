package com.generallycloud.nio.buffer;

import java.nio.ByteBuffer;

public class UnpooledMemoryPool {

	public static ByteBuf allocate(int capacity) {
		return new UnpooledHeapByteBuf(new byte[capacity]);
	}

	public static ByteBuf wrap(ByteBuffer buffer) {
		if (buffer.isDirect()) {
			return new UnpooledDirectByteBuf(buffer);
		}

		return new UnpooledHeapByteBuf(buffer.array());
	}

	public static ByteBuf allocateDirect(int capacity) {
		return new UnpooledDirectByteBuf(ByteBuffer.allocateDirect(capacity));
	}

	static class UnpooledHeapByteBuf extends HeapByteBuf {

		protected UnpooledHeapByteBuf(byte[] memory) {
			super(memory);
		}

		public void release() {

		}

		public ByteBuf duplicate() {
			return new UnpooledHeapByteBuf(array());
		}
	}

	static class UnpooledDirectByteBuf extends DirectByteBuf {

		protected UnpooledDirectByteBuf(ByteBuffer memory) {
			super(memory);
		}

		public void release() {

		}

		public ByteBuf duplicate() {
			return new UnpooledDirectByteBuf(nioBuffer);
		}
	}

}
