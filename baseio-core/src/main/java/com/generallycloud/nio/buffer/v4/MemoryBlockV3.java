package com.generallycloud.nio.buffer.v4;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.buffer.ByteBufferPool;
import com.generallycloud.nio.buffer.ByteProcessor;
import com.generallycloud.nio.buffer.ReferenceCount;
import com.generallycloud.nio.buffer.ReleasedException;
import com.generallycloud.nio.component.SocketChannel;

public class MemoryBlockV3 implements ByteBuf {

	private int			capacity;
	private int			limit;
	private ByteBuffer		memory;
	private ByteBufferPool	memoryPool;
	private int			offset;
	private int			position;
	private ReferenceCount	referenceCount;
	private boolean		released;
	private int			size;
	private ReentrantLock	lock;

	protected MemoryUnitV3	memoryStart;
	protected MemoryUnitV3	memoryEnd;

	protected MemoryBlockV3(ByteBuffer memory) {
		this.memory = memory;
		this.capacity = memory.capacity();
		this.limit = memory.limit();
		this.position = memory.position();
	}

	public MemoryBlockV3(ByteBufferPool byteBufferPool, ByteBuffer memory) {
		this(byteBufferPool, memory, new ReferenceCount());
	}

	public MemoryBlockV3(ByteBufferPool byteBufferPool, ByteBuffer memory, ReferenceCount referenceCount) {
		this.memory = memory;
		this.memoryPool = byteBufferPool;
		this.lock = new ReentrantLock();
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

			MemoryBlockV3 block = new MemoryBlockV3(memoryPool
					, memory.duplicate()
					, referenceCount);

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
		this.memory.limit(ix(position));
		this.memory.position(offset);
		this.limit = position;
		this.position = 0;
		return this;
	}
	
	private int ix(int index){
		return offset + index;
	}

	public byte get(int index) {
		return memory.get(index);
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
		int v = memory.getInt();
		this.position += 4;
		return v;
	}

	public int getInt(int index) {
		return memory.getInt(index);
	}

	public long getLong() {
		long v = memory.getLong();
		this.position += 8;
		return v;
	}

	public long getLong(int index) {
		return memory.getLong(index);
	}

	public boolean hasArray() {
		return memory.hasArray();
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
		this.memory.limit(ix(limit));
		this.memory.position(offset);
		this.limit = limit;
		this.position = 0;
		return this;
	}

	public int position() {
		return position;
	}

	public ByteBuf position(int position) {
		this.memory.position(ix(position));
		this.position = position;
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

	public int read(ByteBuffer buffer) throws IOException {

		int srcRemaining = buffer.remaining();

		if (srcRemaining == 0) {
			return 0;
		}

		int remaining = this.remaining();

		if (remaining <= srcRemaining) {

			buffer.get(this.memory.array(), offset + position, remaining);

			this.position(this.limit);

			return remaining;
		} else {

			buffer.get(this.memory.array(), offset + position, srcRemaining);

			this.position(this.position + srcRemaining);

			return srcRemaining;
		}
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

	protected ByteBuf produce(int newLimit) {
		this.offset = memoryStart.index * memoryPool.getUnitMemorySize();
		this.capacity = size * memoryPool.getUnitMemorySize();
		this.limit = newLimit;
		this.memory.limit(offset + newLimit);
		this.memory.position(offset);
		return this;
	}

	public int write(SocketChannel channel) throws IOException {

		int length = channel.write(memory);

		if (length > 0) {

			position += length;

			channel.upNetworkState();

		} else {

			channel.downNetworkState();
		}

		return length;
	}

	public byte get() {

		byte b = memory.get();

		position++;

		return b;
	}

	public int forEachByte(ByteProcessor processor) {
		return forEachByte(position, limit, processor);
	}

	public int forEachByte(int index, int length, ByteProcessor processor) {
		
		byte [] array = this.array();
		
		int start = ix(index);
		
		int end = start + length;

		try {
			
			for (int i = start; i < end; i++) {
				
				if (!processor.process(array[i])) {
					
					return i - start;
				}
				
			}
			
		} catch (Exception e) {
		}
		
		return -1;
	}

	public int forEachByteDesc(ByteProcessor processor) {
		return forEachByteDesc(position, limit, processor);
	}

	public int forEachByteDesc(int index, int length, ByteProcessor processor) {

		byte [] array = this.array();
		
		int start = ix(index);
		
		int end = start + length;

		try {
			
			for (int i = end; i >= start; i--) {
				
				if (!processor.process(array[i])) {
					
					return i - start;
				}
				
			}
			
		} catch (Exception e) {
		}
		
		return -1;
	}

	public void skipBytes(int length) {
		this.position(position + length);
	}

	public void put(byte b) {
		memory.put(b);
		position++;
	}

}
