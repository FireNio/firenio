package com.generallycloud.nio.buffer;

import java.nio.ByteBuffer;

import com.generallycloud.nio.AbstractLifeCycle;
import com.generallycloud.nio.common.ByteBufferUtil;
import com.generallycloud.nio.common.ReleaseUtil;

public class UnpooledByteBufAllocator extends AbstractLifeCycle implements ByteBufAllocator {

	private static UnpooledByteBufAllocator allocator = new UnpooledByteBufAllocator();

	public static UnpooledByteBufAllocator getInstance() {
		return allocator;
	}

	protected void doStart() throws Exception {

	}

	protected void doStop() throws Exception {

	}

	public ByteBuf allocate(int capacity) {
		return new UnpooledHeapByteBuf(new byte[capacity]);
	}

	public ByteBuf wrap(ByteBuffer buffer) {
		if (buffer.isDirect()) {
			return new UnpooledDirectByteBuf(buffer);
		}
		return wrap(buffer.array(), buffer.position(), buffer.remaining());
	}

	public ByteBuf wrap(byte[] data) {
		return wrap(data, 0, data.length);
	}

	public ByteBuf wrap(byte[] data, int offset, int length) {
		UnpooledHeapByteBuf buf = new UnpooledHeapByteBuf(data);
		buf.offset = offset;
		buf.capacity = length;
		buf.limit = length;
		return buf;
	}

	public ByteBuf allocateDirect(int capacity) {
		return new UnpooledDirectByteBuf(ByteBuffer.allocateDirect(capacity));
	}

	class UnpooledHeapByteBuf extends HeapByteBuf {

		protected UnpooledHeapByteBuf(byte[] memory) {
			super(getInstance(), memory);
			this.produce(memory.length);
		}

		protected void produce(int capacity) {
			this.capacity = capacity;
			this.limit = capacity;
			this.referenceCount = 1;
		}

		public void doRelease() {
			this.memory = null;
		}

		/**
		 * NOTICE 该方法非线程安全
		 */
		public ByteBuf doDuplicate() {

			UnpooledHeapByteBuf buf = new UnpooledHeapByteBuf(memory);

			buf.limit = limit;
			buf.offset = offset;
			buf.position = position;

			return new DuplicateByteBuf(buf, this);
		}

		public void reallocate(int limit) {
			ReleaseUtil.release(this);
			this.memory = new byte[limit];
			this.capacity = limit;
			this.limit = limit;
			this.position = 0;
			this.referenceCount = 1;
		}
	}

	class UnpooledDirectByteBuf extends DirectByteBuf {

		protected UnpooledDirectByteBuf(ByteBuffer memory) {
			super(getInstance(), memory);
			this.produce(memory.capacity());
		}

		protected void produce(int capacity) {
			this.capacity = capacity;
			this.limit = capacity;
			this.referenceCount = 1;
		}

		/**
		 * NOTICE 该方法非线程安全
		 */
		protected ByteBuf doDuplicate() {

			UnpooledDirectByteBuf buf = new UnpooledDirectByteBuf(memory.duplicate());

			buf.beginUnit = beginUnit;
			buf.limit = limit;
			buf.offset = offset;
			buf.position = position;

			return new DuplicateByteBuf(buf, this);
		}

		protected void doRelease() {
			ByteBufferUtil.release(memory);
		}

		public void reallocate(int limit) {
			ReleaseUtil.release(this);
			this.memory = ByteBuffer.allocateDirect(limit);
			this.capacity = limit;
			this.limit = limit;
			this.position = 0;
			this.referenceCount = 1;
		}
	}

	public void release(ByteBuf buf) {

	}

	public int getUnitMemorySize() {
		return 0;
	}

	public void freeMemory() {

	}

	public int getCapacity() {
		return 0;
	}

	public boolean isDirect() {
		return false;
	}

	public void reallocate(ByteBuf buf, int limit) {
		throw new UnsupportedOperationException();
	}

	public void reallocate(ByteBuf buf, int limit, int maxLimit) {
		throw new UnsupportedOperationException();
	}

	public void reallocate(ByteBuf buf, int limit, boolean copyOld) {
		throw new UnsupportedOperationException();
	}

	public void reallocate(ByteBuf buf, int limit, int maxLimit, boolean copyOld) {
		throw new UnsupportedOperationException();
	}

}
