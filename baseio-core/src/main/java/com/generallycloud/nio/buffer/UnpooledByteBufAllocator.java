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

public class UnpooledByteBufAllocator extends AbstractLifeCycle implements ByteBufAllocator {

	private boolean isDirect;
	
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
	public boolean isDirect() {
		return isDirect;
	}

	@Override
	public ByteBuf reallocate(ByteBuf buf, int limit) {
		return reallocate(buf, limit, false);
	}

	@Override
	public ByteBuf reallocate(ByteBuf buf, int limit, int maxLimit) {
		if (limit > maxLimit) {
			throw new BufferException("limit:"+limit+",maxLimit:"+maxLimit);
		}
		return reallocate(buf, limit);
	}

	@Override
	public ByteBuf reallocate(ByteBuf buf, int limit, boolean copyOld) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ByteBuf reallocate(ByteBuf buf, int limit, int maxLimit, boolean copyOld) {
		if (limit > maxLimit) {
			throw new BufferException("limit:"+limit+",maxLimit:"+maxLimit);
		}
		return reallocate(buf,limit,copyOld);
	}
	
	@Override
	public ByteBuf allocate(int limit, int maxLimit) {
		if (limit > maxLimit) {
			throw new BufferException("limit:"+limit+",maxLimit:"+maxLimit);
		}
		return allocate(limit);
	}

}
