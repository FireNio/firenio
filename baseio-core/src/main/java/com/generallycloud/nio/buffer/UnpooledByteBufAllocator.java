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

public class UnpooledByteBufAllocator extends AbstractByteBufAllocator {

	public UnpooledByteBufAllocator(boolean isDirect) {
		super(isDirect);
	}

	private static UnpooledByteBufAllocator allocator;
	
	private UnpooledByteBufFactory unpooledByteBufferFactory;
	
	private UnpooledByteBufFactory unpooledDirectByteBufFactory;

	public static UnpooledByteBufAllocator getInstance() {
		return allocator;
	}

	@Override
	public ByteBuf allocate(int capacity) {
		return unpooledByteBufferFactory.allocate(capacity);
	}

	public ByteBuf wrap(ByteBuffer buffer) {
		if (buffer.isDirect()) {
			return new UnpooledDirectByteBuf(buffer);
		}
		return wrap(buffer.array(), buffer.position(), buffer.remaining());
	}
	
	@Override
	protected void doStart() throws Exception {
		unpooledDirectByteBufFactory = new UnpooledDirectByteBufferFactory();
		if (isDirect) {
			unpooledByteBufferFactory = unpooledDirectByteBufFactory;
			return;
		}
		unpooledByteBufferFactory = new UnpooledHeapByteBufferFactory();
		allocator = this;
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
		return unpooledDirectByteBufFactory.allocate(capacity);
	}

	@Override
	public void release(ByteBuf buf) {
		
	}

	@Override
	public int getUnitMemorySize() {
		return -1;
	}

	@Override
	public void freeMemory() {
		
	}

	@Override
	public int getCapacity() {
		return -1;
	}

	@Override
	public ByteBuf reallocate(ByteBuf buf, int limit, boolean copyOld) {
		throw new UnsupportedOperationException();
	}
	
	interface UnpooledByteBufFactory{
		abstract ByteBuf allocate(int capacity);
	}
	
	class UnpooledHeapByteBufferFactory implements UnpooledByteBufFactory{
		@Override
		public ByteBuf allocate(int capacity) {
			return new UnpooledHeapByteBuf(new byte[capacity]);
		}
	}
	
	class UnpooledDirectByteBufferFactory implements UnpooledByteBufFactory{
		@Override
		public ByteBuf allocate(int capacity) {
			return new UnpooledDirectByteBuf(ByteBuffer.allocateDirect(capacity));
		}
	}

}
