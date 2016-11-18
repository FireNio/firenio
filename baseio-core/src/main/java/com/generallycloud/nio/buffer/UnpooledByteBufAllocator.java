package com.generallycloud.nio.buffer;

import java.nio.ByteBuffer;

import com.generallycloud.nio.AbstractLifeCycle;

public class UnpooledByteBufAllocator extends AbstractLifeCycle implements ByteBufAllocator{

	private static UnpooledByteBufAllocator allocator = new UnpooledByteBufAllocator();
	
	public static UnpooledByteBufAllocator getInstance(){
		return allocator;
	} 
	
	protected void doStart() throws Exception {
		
	}

	protected void doStop() throws Exception {
		
	}

	public  ByteBuf allocate(int capacity) {
		return new UnpooledHeapByteBuf(new byte[capacity]);
	}

	public  ByteBuf wrap(ByteBuffer buffer) {
		if (buffer.isDirect()) {
			return new UnpooledDirectByteBuf(buffer);
		}
		return wrap(buffer.array(), buffer.position(), buffer.remaining());
	}
	
	public  ByteBuf wrap(byte [] data) {
		return wrap(data, 0, data.length);
	}
	
	public  ByteBuf wrap(byte [] data,int offset,int length) {
		UnpooledHeapByteBuf buf = new UnpooledHeapByteBuf(data);
		buf.offset = offset;
		buf.capacity = length;
		buf.limit = length;
		return buf;
	}

	public  ByteBuf allocateDirect(int capacity) {
		return new UnpooledDirectByteBuf(ByteBuffer.allocateDirect(capacity));
	}
	
	 class UnpooledHeapByteBuf extends HeapByteBuf {

		protected UnpooledHeapByteBuf(byte[] memory) {
			super(memory);
		}

		public void release() {

		}

		/**
		 * NOTICE 该方法非线程安全
		 */
		public ByteBuf duplicate() {

			UnpooledHeapByteBuf buf = new UnpooledHeapByteBuf(memory);
			
			buf.limit = limit;
			buf.offset = offset;
			buf.position = position;
			
			return new DuplicateByteBuf(buf, this);
		}
	}

	 class UnpooledDirectByteBuf extends DirectByteBuf {

		protected UnpooledDirectByteBuf(ByteBuffer memory) {
			super(memory);
		}

		public void release() {

		}

		/**
		 * NOTICE 该方法非线程安全
		 */
		public ByteBuf duplicate() {

			UnpooledDirectByteBuf buf = new UnpooledDirectByteBuf(memory.duplicate());
			
			buf.beginUnit = beginUnit;
			buf.limit = limit;
			buf.offset = offset;
			buf.position = position;
			
			return new DuplicateByteBuf(buf, this);
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

}
