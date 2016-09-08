package com.generallycloud.nio.buffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.component.TCPEndPoint;

public class MemoryBlock implements PooledByteBuf {

	private PooledByteBuf	previous;

	private PooledByteBuf	next;

	private MemoryUnit	start;

	private int		size;

	private MemoryUnit	end;

	private ByteBufferPool	memoryPool;

	private ByteBuffer	memory;

	private int		offset;

	public void setMemory(MemoryUnit start, MemoryUnit end) {
		this.start = start;
		this.end = end;
		this.size = end.getIndex() - start.getIndex() + 1;
	}

	public MemoryUnit getStart() {
		return start;
	}

	public int getSize() {
		return size;
	}

	public MemoryUnit getEnd() {
		return end;
	}

	public PooledByteBuf getPrevious() {
		return previous;
	}

	public void setPrevious(PooledByteBuf previous) {
		this.previous = previous;
	}

	public PooledByteBuf getNext() {
		return next;
	}

	public void setNext(PooledByteBuf next) {
		this.next = next;
	}

	public PooledByteBuf use() {
		this.start.setUsing(true);
		this.end.setUsing(true);
		this.offset = start.getIndex() * memoryPool.getUnitMemorySize();
		this.capacity = size * memoryPool.getUnitMemorySize();
		this.limit = this.capacity;
		return this;
	}

	public PooledByteBuf free() {
		this.start.setUsing(false);
		this.end.setUsing(false);
		return this;
	}

	public boolean using() {
		return this.start.isUsing();
	}

	private int			capacity;

	private int			position;

	private int			limit;

	private boolean		released;

	private ReferenceCount	referenceCount;

	private ReentrantLock	lock	= new ReentrantLock();

	public MemoryBlock(ByteBufferPool byteBufferPool,ByteBuffer memory) {
		this.memory = memory;
		this.memoryPool = byteBufferPool;
		this.referenceCount = new ReferenceCount();
	}

	public void release() {

		ReentrantLock lock = this.lock;

		lock.lock();

		try {

			if (released) {
				throw new ReleasedException("released");
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

	public int read(TCPEndPoint endPoint) throws IOException {

		int read = endPoint.read(memory);

		position += read;

		return read;
	}

	public int write(TCPEndPoint endPoint) throws IOException {

		int read = endPoint.write(memory);

		position += read;

		return read;
	}

	public void getBytes(byte[] dst) {
		getBytes(dst, 0, dst.length);
	}

	// FIXME offset buwei0shiyouwenti
	public void getBytes(byte[] dst, int offset, int length) {
		memory.get(dst, offset, length);
	}

	public void putBytes(byte[] src) {
		putBytes(src, 0, src.length);
	}

	// FIXME offset buwei0shiyouwenti
	public void putBytes(byte[] src, int offset, int length) {
		memory.put(src, offset, length);
	}

	public void flip() {
		memory.limit(offset + position).position(offset);
		limit = position;
		position = offset;
	}

	public PooledByteBuf duplicate() {

		// FIXME .......................
		ReentrantLock lock = this.lock;

		lock.lock();

		try {

			if (released) {
				throw new ReleasedException("released");
			}

			MemoryBlock block = new MemoryBlock(memoryPool,memory.duplicate());

			block.referenceCount = referenceCount;
			block.referenceCount.increament();

			return block;

		} finally {
			lock.unlock();
		}
	}

	public int remaining() {
		return limit - position;
	}

	public int position() {
		return position;
	}

	public void position(int position) {
		this.position = position;
	}

	public int limit() {
		return limit;
	}

	public void limit(int limit) {
		this.limit = limit;
		memory.limit(offset + limit).position(offset);
	}

	public int capacity() {
		return capacity;
	}

	public boolean hasRemaining() {
		return remaining() > 0;
	}

	public boolean hasArray() {
		return memory.hasArray();
	}

	public void clear() {
		this.position = 0;
		this.limit = capacity;
		memory.position(offset).limit(limit);
	}

	public void touch() {

	}

	public int getInt() {
		return memory.getInt(offset);
	}

	public long getLong() {
		return memory.getLong(offset);
	}

	public int getInt(int index) {
		return memory.getInt(offset + index);
	}

	public long getLong(int index) {
		return memory.getLong(offset + index);
	}

	public byte[] array() {
		return memory.array();
	}
}
