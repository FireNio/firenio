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

import java.io.IOException;
import java.nio.ByteBuffer;

import com.generallycloud.nio.component.SocketChannel;

public abstract class AbstractByteBuf extends AbstractPooledByteBuf {

	protected ByteBufAllocator	allocator;
	protected int				capacity;
	protected int				limit;
	protected ByteBuffer		nioBuffer;
	protected int				offset;
	protected int				position		= 0;
	protected int				referenceCount	= 0;

	protected AbstractByteBuf(ByteBufAllocator allocator) {
		this.allocator = allocator;
	}

	@Override
	public ByteBuf duplicate() {

		synchronized (this) {

			if (released) {
				throw new ReleasedException("");
			}

			this.referenceCount++;

			return doDuplicate();
		}
	}
	
	protected ByteBuf doDuplicate() {
		
		AbstractByteBuf buf = newByteBuf();
		
		buf.beginUnit = beginUnit;
		buf.limit = limit;
		buf.offset = offset;
		buf.position = position;

		return new DuplicateByteBuf(buf, this);
	}

	protected abstract AbstractByteBuf newByteBuf();

	@Override
	public int capacity() {
		return capacity;
	}

	@Override
	public ByteBuf clear() {
		this.position = 0;
		this.limit = capacity;
		return this;
	}

	@Override
	public ByteBuf flip() {
		this.limit = position;
		this.position = 0;
		return this;
	}

	@Override
	public int forEachByte(ByteProcessor processor) {
		return forEachByte(position, limit, processor);
	}

	@Override
	public int forEachByteDesc(ByteProcessor processor) {
		return forEachByteDesc(position, limit, processor);
	}

	@Override
	public void get(byte[] dst) {
		get(dst, 0, dst.length);
	}

	@Override
	public byte[] getBytes() {

		byte[] bytes = new byte[remaining()];

		get(bytes);

		return bytes;
	}

	protected abstract ByteBuffer getNioBuffer();

	@Override
	public boolean hasRemaining() {
		return position < limit;
	}

	protected int ix(int index) {
		return offset + index;
	}

	@Override
	public int limit() {
		return limit;
	}

	/**
	 * 注意，该方法会重置position
	 */
	@Override
	public ByteBuf limit(int limit) {
		this.limit = limit;
		this.position = 0;
		return this;
	}

	@Override
	public ByteBuffer nioBuffer() {
		ByteBuffer buffer = getNioBuffer();
		return (ByteBuffer) buffer.limit(ix(limit)).position(ix(position));
	}

	@Override
	public int offset() {
		return offset;
	}

	protected void offset(int offset) {
		this.offset = offset;
	}

	@Override
	public int position() {
		return position;
	}

	@Override
	public ByteBuf position(int position) {
		this.position = position;
		return this;
	}

	@Override
	public PooledByteBuf produce(int begin, int end, int newLimit) {
		this.offset = begin * allocator.getUnitMemorySize();
		this.capacity = (end - begin) * allocator.getUnitMemorySize();
		this.limit = newLimit;
		this.position = 0;
		this.beginUnit = begin;
		this.referenceCount = 1;
		return this;
	}
	
	@Override
	public PooledByteBuf produce(PooledByteBuf buf) {
		this.offset = buf.offset();
		this.capacity = buf.capacity();
		this.limit = buf.limit();
		this.position = buf.position();
		this.beginUnit = buf.getBeginUnit();
		this.referenceCount = 1;
		this.released = false;
		return this;
	}

	@Override
	public void put(byte[] src) {
		put(src, 0, src.length);
	}

	@Override
	public int read(SocketChannel channel) throws IOException {

		int length = channel.read(getNioBuffer());

		if (length > 0) {
			position += length;
		}

		return length;
	}

	@Override
	public void release() {

		synchronized (this) {

			if (released) {
				return;
			}

			if (--referenceCount != 0) {
				return;
			}

			released = true;

			doRelease();
		}
	}
	

	@Override
	public int read(ByteBuffer buffer) {

		int srcRemaining = buffer.remaining();

		if (srcRemaining == 0) {
			return 0;
		}

		int remaining = this.remaining();
		
		if (remaining == 0) {
			return 0;
		}

		return read0(buffer, srcRemaining, remaining);
	}
	
	public abstract int read0(ByteBuffer buffer,int srcRemaining,int remaining) ;
	
	@Override
	public int read(ByteBuf buf) {

		int srcRemaining = buf.remaining();

		if (srcRemaining == 0) {
			return 0;
		}

		int remaining = this.remaining();
		
		if (remaining == 0) {
			return 0;
		}

		return read0(buf, srcRemaining, remaining);
	}
	
	public abstract int read0(ByteBuf buf,int srcRemaining,int remaining) ;
	
	protected void doRelease(){
		allocator.release(this);
	}

	@Override
	public int remaining() {
		return limit - position;
	}

	@Override
	public ByteBuf skipBytes(int length) {
		return position(position + length);
	}

	@Override
	public ByteBuf reallocate(int limit) {
		return reallocate(limit, false);
	}
	
	@Override
	public ByteBuf reallocate(int limit, boolean copyOld) {
		return allocator.reallocate(this, limit, copyOld);
	}

	@Override
	public ByteBuf reallocate(int limit, int maxLimit, boolean copyOld) {
		
		if (limit < 1) {
			throw new BufferException("illegal limit:" + limit);
		}
		
		if (limit > maxLimit) {
			throw new BufferException("limit:" + limit +",maxLimit:"+maxLimit);
		}
		return reallocate(limit,copyOld);
	}

	@Override
	public ByteBuf reallocate(int limit, int maxLimit) {
		return reallocate(limit, maxLimit, false);
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(this.getClass().getName());
		b.append("[pos=");
		b.append(position);
		b.append(",lim=");
		b.append(limit);
		b.append(",cap=");
		b.append(capacity);
		b.append(",remaining=");
		b.append(remaining());
		b.append(",offset=");
		b.append(offset);
		b.append("]");
		return b.toString();
	}

}
