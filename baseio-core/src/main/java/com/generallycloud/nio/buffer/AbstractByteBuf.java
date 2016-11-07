package com.generallycloud.nio.buffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.component.SocketChannel;

public abstract class AbstractByteBuf implements ByteBuf {

	protected int			capacity;
	protected int			limit;
	protected ByteBufferPool	memoryPool;
	protected int			offset;
	protected int			position;
	protected ReferenceCount	referenceCount;
	protected boolean		released;
	protected int			size;
	protected ReentrantLock	lock;
	protected ByteBuffer	nioBuffer;

	protected MemoryUnit	memoryStart;
	protected MemoryUnit	memoryEnd;

	protected AbstractByteBuf(int capacity) {
		this.capacity = capacity;
		this.limit = capacity;
		this.position = 0;
	}

	protected AbstractByteBuf(ByteBufferPool byteBufferPool) {
		this(byteBufferPool, new ReferenceCount());
	}

	protected AbstractByteBuf(ByteBufferPool byteBufferPool, ReferenceCount referenceCount) {
		this.limit = capacity;
		this.position = 0;
		this.memoryPool = byteBufferPool;
		this.lock = new ReentrantLock();
		this.referenceCount = referenceCount;
	}

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

	protected int ix(int index) {
		return offset + index;
	}

	public byte[] getBytes() {

		byte[] bytes = new byte[remaining()];

		get(bytes);

		return bytes;
	}

	public void get(byte[] dst) {
		get(dst, 0, dst.length);
	}

	public boolean hasRemaining() {
		return position < limit;
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

	public int position() {
		return position;
	}

	public ByteBuf position(int position) {
		this.position = position;
		return this;
	}

	public void put(byte[] src) {
		put(src, 0, src.length);
	}

	public ByteBuffer nioBuffer() {
		ByteBuffer buffer = getNioBuffer();
		return (ByteBuffer) buffer.limit(ix(limit)).position(ix(position));
	}

	protected abstract ByteBuffer getNioBuffer();

	public int read(SocketChannel channel) throws IOException {

		int length = channel.read(getNioBuffer());

		if (length > 0) {
			position += length;
		}

		return length;
	}

	public void release() {

		ReentrantLock lock = this.lock;

		lock.lock();

		try {

			if (released) {
				return;
			}

			if (referenceCount.deincreament() > 0) {
				return;
			}

			released = true;

			memoryPool.release(this);

		} finally {
			lock.unlock();
		}
	}

	public int offset() {
		return offset;
	}

	public int remaining() {
		return limit - position;
	}

	protected void setMemory(MemoryUnit memoryStart, MemoryUnit memoryEnd) {
		this.memoryStart = memoryStart;
		this.memoryEnd = memoryEnd;
		this.size = memoryStart.blockEnd - memoryStart.index;
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

	protected ByteBuf produce(int newLimit) {
		this.offset = memoryStart.index * memoryPool.getUnitMemorySize();
		this.capacity = size * memoryPool.getUnitMemorySize();
		this.limit = newLimit;
		return this;
	}

	public int forEachByte(ByteProcessor processor) {
		return forEachByte(position, limit, processor);
	}

	public int forEachByteDesc(ByteProcessor processor) {
		return forEachByteDesc(position, limit, processor);
	}

	public void skipBytes(int length) {
		this.position(position + length);
	}

}
