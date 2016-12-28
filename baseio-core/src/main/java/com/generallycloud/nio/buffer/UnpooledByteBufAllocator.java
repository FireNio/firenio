/*
 * Copyright 2015 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
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

	@Override
	protected void doStart() throws Exception {

	}

	@Override
	protected void doStop() throws Exception {

	}

	@Override
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

		@Override
		public void doRelease() {
			this.memory = null;
		}

		/**
		 * NOTICE 该方法非线程安全
		 */
		@Override
		public ByteBuf doDuplicate() {

			UnpooledHeapByteBuf buf = new UnpooledHeapByteBuf(memory);

			buf.limit = limit;
			buf.offset = offset;
			buf.position = position;

			return new DuplicateByteBuf(buf, this);
		}

		@Override
		public ByteBuf reallocate(int limit) {
			ReleaseUtil.release(this);
			this.memory = new byte[limit];
			this.capacity = limit;
			this.limit = limit;
			this.position = 0;
			this.referenceCount = 1;
			return this;
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
		@Override
		protected ByteBuf doDuplicate() {

			UnpooledDirectByteBuf buf = new UnpooledDirectByteBuf(memory.duplicate());

			buf.beginUnit = beginUnit;
			buf.limit = limit;
			buf.offset = offset;
			buf.position = position;

			return new DuplicateByteBuf(buf, this);
		}

		@Override
		protected void doRelease() {
			ByteBufferUtil.release(memory);
		}

		@Override
		public ByteBuf reallocate(int limit) {
			ReleaseUtil.release(this);
			this.memory = ByteBuffer.allocateDirect(limit);
			this.capacity = limit;
			this.limit = limit;
			this.position = 0;
			this.referenceCount = 1;
			return this;
		}
	}

	@Override
	public void release(ByteBuf buf) {

	}

	@Override
	public int getUnitMemorySize() {
		return 0;
	}

	@Override
	public void freeMemory() {

	}

	@Override
	public int getCapacity() {
		return 0;
	}

	@Override
	public boolean isDirect() {
		return false;
	}

	@Override
	public ByteBuf reallocate(ByteBuf buf, int limit) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ByteBuf reallocate(ByteBuf buf, int limit, int maxLimit) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ByteBuf reallocate(ByteBuf buf, int limit, boolean copyOld) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ByteBuf reallocate(ByteBuf buf, int limit, int maxLimit, boolean copyOld) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public ByteBuf allocate(int limit, int maxLimit) {
		if (limit > maxLimit) {
			throw new BufferException("limit:"+limit+",maxLimit:"+maxLimit);
		}
		return allocate(limit);
	}

}
