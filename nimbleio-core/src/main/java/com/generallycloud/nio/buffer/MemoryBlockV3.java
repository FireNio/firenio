package com.generallycloud.nio.buffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.component.SocketChannel;

public class MemoryBlockV3 implements ByteBuf {

	private int			capacity;
	public MemoryUnitV3		memoryEnd;
	private int			limit;
	private ReentrantLock	lock	= new ReentrantLock();
	private ByteBuffer		memory;
	private ByteBufferPool	memoryPool;
	private int			offset;
	private int			position;
	private ReferenceCount	referenceCount;
	private boolean		released;
	private int			size;
	public MemoryUnitV3		memoryStart;

	public MemoryBlockV3(ByteBufferPool byteBufferPool, ByteBuffer memory) {
		this.memory = memory;
		this.memoryPool = byteBufferPool;
		this.referenceCount = new ReferenceCount();
	}

	public MemoryBlockV3(ByteBufferPool byteBufferPool, ByteBuffer memory, ReferenceCount referenceCount) {
		this.memory = memory;
		this.memoryPool = byteBufferPool;
		this.referenceCount = referenceCount;
	}

	public byte[] array() {
		return memory.array();
	}

	public int capacity() {
		return capacity;
	}

	public ByteBuf clear() {
		this.position = 0;
		this.limit = capacity;
		memory.position(offset).limit(limit);
		return this;
	}

	public ByteBuf duplicate() {

		ReentrantLock lock = this.lock;

		lock.lock();

		try {

			if (released) {
				throw new ReleasedException("released");
			}

			MemoryBlockV3 block = new MemoryBlockV3(memoryPool, memory.duplicate(), referenceCount);

			block.referenceCount.increament();
			block.capacity = capacity;
			block.memoryEnd = memoryEnd;
			block.limit = limit;
			block.offset = offset;
			block.position = position;
			block.size = size;
			block.memoryStart = memoryStart;

			return block;

		} finally {
			lock.unlock();
		}
	}

	public ByteBuf flip() {
		memory.limit(offset + position).position(offset);
		limit = position;
		position = 0;
		return this;
	}

	public byte get(int index) {
		return memory.get(offset + index);
	}

	public ByteBuffer getMemory() {
		return memory;
	}

	public byte[] getBytes() {

		byte[] bytes = new byte[limit];

		get(bytes);

		return bytes;
	}

	public void get(byte[] dst) {
		get(dst, 0, dst.length);
	}

	public void get(byte[] dst, int offset, int length) {
		this.memory.get(dst, offset, length);
		this.position += (length - offset);
	}

	public int getInt() {
		return memory.getInt(offset);
	}

	public int getInt(int index) {
		return memory.getInt(offset + index);
	}

	public long getLong() {
		return memory.getLong(offset);
	}

	public long getLong(int index) {
		return memory.getLong(offset + index);
	}

	public int getSize() {
		return size;
	}

	public boolean hasArray() {
		return memory.hasArray();
	}

	public boolean hasRemaining() {
		return remaining() > 0;
	}

	public int limit() {
		return limit;
	}

	public ByteBuf limit(int limit) {
		this.limit = limit;
		this.position = 0;
		memory.limit(offset + limit).position(offset);
		return this;
	}

	public int position() {
		return position;
	}

	public ByteBuf position(int position) {
		this.position = position;
		this.memory.position(offset + position);
		return this;
	}

	public void put(byte[] src) {
		put(src, 0, src.length);
	}

	public void put(byte[] src, int offset, int length) {
		this.memory.put(src, offset, length);
		this.position += (length - offset);
	}

	public int read(SocketChannel channel) throws IOException {

		int length = channel.read(memory);

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

	public void setMemory(MemoryUnitV3 memoryStart, MemoryUnitV3 memoryEnd) {
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

	public ByteBuf use() {
		this.offset = memoryStart.index * memoryPool.getUnitMemorySize();
		this.capacity = size * memoryPool.getUnitMemorySize();
		this.limit = this.capacity;
		return this;
	}

	public int write(SocketChannel channel) throws IOException {

		int length = channel.write(memory);

		if (length > 0) {
			position += length;
		}

		return length;
	}

}
