package com.generallycloud.nio.buffer;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.generallycloud.nio.component.SocketChannel;

public abstract class AbstractByteBuf implements ByteBuf {

	protected ByteBufAllocator	allocator;
	protected int				capacity;
	protected int				limit;
	protected ByteBuffer		nioBuffer;
	protected int				offset;
	protected int				position;
	protected boolean			released;
	protected int				index;
	protected boolean			free;
	protected int				blockEnd;
	protected int				blockBegin;
	protected ReferenceCount	referenceCount;

	protected AbstractByteBuf(ByteBufAllocator allocator) {
		this(allocator, new ReferenceCount());
	}

	protected AbstractByteBuf(ByteBufAllocator allocator, ReferenceCount referenceCount) {
		this.limit = capacity;
		this.position = 0;
		this.allocator = allocator;
		this.referenceCount = referenceCount;
	}

	protected AbstractByteBuf(int capacity) {
		this.capacity = capacity;
		this.limit = capacity;
		this.position = 0;
	}

	public ByteBuf duplicate() {

		synchronized (this) {

			if (released) {
				throw new ReleasedException("released");
			}

			AbstractByteBuf buf = newByteBuf();

			buf.referenceCount.increament();
			buf.blockEnd = blockEnd;
			buf.free = free;
			buf.index = index;
			buf.limit = limit;
			buf.nioBuffer = nioBuffer;
			buf.offset = offset;
			buf.position = position;
			buf.released = released;
			
			return new DuplicateByteBuf(buf, this);
		}
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

	public int position() {
		return position;
	}

	public ByteBuf position(int position) {
		this.position = position;
		return this;
	}

	protected ByteBuf produce(int newLimit) {
		this.offset = index * allocator.getUnitMemorySize();
		this.capacity = (blockEnd - index) * allocator.getUnitMemorySize();
		this.limit = newLimit;
		this.position = 0;
		this.released = false;
		this.referenceCount.increament();
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

			if (referenceCount.deincreament() != 0) {
				return;
			}

			released = true;

			allocator.release(this);

		}
	}

	public int remaining() {
		return limit - position;
	}

	public void skipBytes(int length) {
		this.position(position + length);
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
		b.append("]");
		return b.toString();
	}

}
