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

	public int capacity() {
		return capacity;
	}

	public ByteBuf clear() {
		this.position = 0;
		this.limit = capacity;
		return this;
	}

	public ByteBuf flip() {
		this.limit = position;
		this.position = 0;
		return this;
	}

	public int forEachByte(ByteProcessor processor) {
		return forEachByte(position, limit, processor);
	}

	public int forEachByteDesc(ByteProcessor processor) {
		return forEachByteDesc(position, limit, processor);
	}

	public void get(byte[] dst) {
		get(dst, 0, dst.length);
	}

	public byte[] getBytes() {

		byte[] bytes = new byte[remaining()];

		get(bytes);

		return bytes;
	}

	protected abstract ByteBuffer getNioBuffer();

	public boolean hasRemaining() {
		return position < limit;
	}

	protected int ix(int index) {
		return offset + index;
	}

	public int limit() {
		return limit;
	}

	/**
	 * 注意，该方法会重置position
	 */
	public ByteBuf limit(int limit) {
		this.limit = limit;
		this.position = 0;
		return this;
	}

	public ByteBuffer nioBuffer() {
		ByteBuffer buffer = getNioBuffer();
		return (ByteBuffer) buffer.limit(ix(limit)).position(ix(position));
	}

	public int offset() {
		return offset;
	}

	protected void offset(int offset) {
		this.offset = offset;
	}

	public int position() {
		return position;
	}

	public ByteBuf position(int position) {
		this.position = position;
		return this;
	}

	public PooledByteBuf produce(int begin, int end, int newLimit) {
		this.offset = begin * allocator.getUnitMemorySize();
		this.capacity = (end - begin) * allocator.getUnitMemorySize();
		this.limit = newLimit;
		this.position = 0;
		this.beginUnit = begin;
		this.referenceCount = 1;
		return this;
	}
	
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

	public void put(byte[] src) {
		put(src, 0, src.length);
	}

	public int read(SocketChannel channel) throws IOException {

		int length = channel.read(getNioBuffer());

		if (length > 0) {
			position += length;
		}

		return length;
	}

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
	
	protected void doRelease(){
		allocator.release(this);
	}

	public int remaining() {
		return limit - position;
	}

	public void skipBytes(int length) {
		this.position(position + length);
	}

	public void reallocate(int limit) {
		reallocate(limit, false);
	}
	
	public void reallocate(int limit, boolean copyOld) {
		allocator.reallocate(this, limit, copyOld);
	}

	public void reallocate(int limit, int maxLimit, boolean copyOld) {
		
		if (limit < 1) {
			throw new BufferException("illegal limit:" + limit);
		}
		
		if (limit > maxLimit) {
			throw new BufferException("limit:" + limit +",maxLimit:"+maxLimit);
		}
		reallocate(limit,copyOld);
	}

	public void reallocate(int limit, int maxLimit) {
		reallocate(limit, maxLimit, false);
	}

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
