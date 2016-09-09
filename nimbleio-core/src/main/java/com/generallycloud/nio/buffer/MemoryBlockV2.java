package com.generallycloud.nio.buffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.component.TCPEndPoint;

public class MemoryBlockV2 implements ByteBuf {

	private int			capacity;
	private int			end;
	private int			limit;
	private ReentrantLock	lock	= new ReentrantLock();
	private ByteBuffer		memory;
	private ByteBufferPool	memoryPool;
	private int			offset;
	private int			position;
	private ReferenceCount	referenceCount;
	private boolean		released;
	private int			size;
	private int			start;

	public MemoryBlockV2(ByteBufferPool byteBufferPool, ByteBuffer memory) {
		this.memory = memory;
		this.memoryPool = byteBufferPool;
		this.referenceCount = new ReferenceCount();
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
	
	public byte get(int index) {
		return memory.get(offset + index);
	}
	
	public byte[] getBytes() {
		
		byte[] bytes = new byte[limit];
		
		getBytes(bytes);
		
		return bytes;
	}

	public ByteBuf duplicate() {

		// FIXME .......................
		ReentrantLock lock = this.lock;

		lock.lock();

		try {

			if (released) {
				throw new ReleasedException("released");
			}

			MemoryBlockV2 block = new MemoryBlockV2(memoryPool, memory.duplicate());

			block.referenceCount = referenceCount;
			block.referenceCount.increament();

			return block;

		} finally {
			lock.unlock();
		}
	}

	public ByteBuf flip() {
		memory.limit(offset + position).position(offset);
		limit = position;
		position = offset;
		return this;
	}

	public void getBytes(byte[] dst) {
		getBytes(dst, 0, dst.length);
	}

	// FIXME offset buwei0shiyouwenti
	public void getBytes(byte[] dst, int offset, int length) {
		memory.get(dst, offset, length);
	}

	public int getEnd() {
		return end;
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

	public int getStart() {
		return start;
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
		memory.limit(offset + limit).position(offset);
		return this;
	}

	public int position() {
		return position;
	}

	public ByteBuf position(int position) {
		this.position = position;
		return this;
	}

	public void putBytes(byte[] src) {
		putBytes(src, 0, src.length);
	}

	// FIXME offset buwei0shiyouwenti
	public void putBytes(byte[] src, int offset, int length) {
		memory.put(src, offset, length);
	}

	public int read(TCPEndPoint endPoint) throws IOException {
		
		int length = -1;

		try {

			length = endPoint.read(memory);
			
			return length;

		} finally {

			if (length < 1) {
				
				this.release();
				
				if (length == -1) {
					CloseUtil.close(endPoint);
				}
			}else{
				
				position += length;
			}
		}
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

	public int remaining() {
		return limit - position;
	}

	public void setMemory(int start, int end) {
		this.start = start;
		this.end = end;
		this.size = end - start;
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

	public void touch() {

	}

	public ByteBuf use() {
		this.offset = start * memoryPool.getUnitMemorySize();
		this.capacity = size * memoryPool.getUnitMemorySize();
		this.limit = this.capacity;
		return this;
	}

	public int write(TCPEndPoint endPoint) throws IOException {

		int length = -1;

		try {

			length = endPoint.write(memory);
			
			return length;

		} finally {

			if (length < 1) {
				
				this.release();
				
				if (length == -1) {
					CloseUtil.close(endPoint);
				}
			}else{
				
				position += length;
			}
		}
	}
	
}
