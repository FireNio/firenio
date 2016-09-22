package com.generallycloud.nio.buffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.component.SocketChannel;

@Deprecated
public class MemoryBlockV1 implements PooledByteBuf {

	private PooledByteBuf	previous;

	private PooledByteBuf	next;

	private MemoryUnitV1	start;

	private int		size;

	private MemoryUnitV1	end;

	private ByteBufferPool	memoryPool;

	private ByteBuffer	memory;

	private int		offset;

	public void setMemory(MemoryUnitV1 start, MemoryUnitV1 end) {
		this.start = start;
		this.end = end;
		this.size = end.getIndex() - start.getIndex() + 1;
	}

	public MemoryUnitV1 getStart() {
		return start;
	}

	public int getSize() {
		return size;
	}
	
	public byte get(int index) {
		return 0;
	}

	public MemoryUnitV1 getEnd() {
		return end;
	}

	public PooledByteBuf getPrevious() {
		return previous;
	}
	
	public ByteBuffer getMemory() {
		return memory;
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
	
	public byte[] getBytes() {
		return null;
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

	public MemoryBlockV1(ByteBufferPool byteBufferPool,ByteBuffer memory) {
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

	public int read(SocketChannel channel) throws IOException {

		int read = channel.read(memory);

		position += read;

		return read;
	}

	public int write(SocketChannel channel) throws IOException {

		int read = channel.write(memory);

		position += read;

		return read;
	}

	public void get(byte[] dst) {
		get(dst, 0, dst.length);
	}

	public void get(byte[] dst, int offset, int length) {
		memory.get(dst, offset, length);
	}

	public void put(byte[] src) {
		put(src, 0, src.length);
	}

	public void put(byte[] src, int offset, int length) {
		memory.put(src, offset, length);
	}

	public PooledByteBuf flip() {
		memory.limit(offset + position).position(offset);
		limit = position;
		position = offset;
		return this;
	}

	public PooledByteBuf duplicate() {

		ReentrantLock lock = this.lock;

		lock.lock();

		try {

			if (released) {
				throw new ReleasedException("released");
			}

			MemoryBlockV1 block = new MemoryBlockV1(memoryPool,memory.duplicate());

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

	public PooledByteBuf position(int position) {
		this.position = position;
		return this;
	}

	public int limit() {
		return limit;
	}

	public PooledByteBuf limit(int limit) {
		this.limit = limit;
		memory.limit(offset + limit).position(offset);
		return this;
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

	public PooledByteBuf clear() {
		this.position = 0;
		this.limit = capacity;
		memory.position(offset).limit(limit);
		return this;
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

	public int offset() {
		return 0;
	}
	
}
