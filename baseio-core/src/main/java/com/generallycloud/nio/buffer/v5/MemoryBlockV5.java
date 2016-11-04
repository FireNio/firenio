package com.generallycloud.nio.buffer.v5;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.buffer.ByteBufferPool;
import com.generallycloud.nio.buffer.ByteProcessor;
import com.generallycloud.nio.buffer.ReferenceCount;
import com.generallycloud.nio.buffer.ReleasedException;
import com.generallycloud.nio.common.MathUtil;
import com.generallycloud.nio.component.SocketChannel;

public class MemoryBlockV5 implements ByteBuf {

	private int			capacity;
	private int			limit;
	private byte[]		array;
	private ByteBufferPool	memoryPool;
	private int			offset;
	private int			position;
	private ReferenceCount	referenceCount;
	private boolean		released;
	private int			size;
	private ReentrantLock	lock;
	private ByteBuffer		nioBuffer;

	protected MemoryUnitV5	memoryStart;
	protected MemoryUnitV5	memoryEnd;

	protected MemoryBlockV5(ByteBuffer memory) {
		this.nioBuffer = memory;
		this.array = memory.array();
		this.capacity = memory.capacity();
		this.limit = capacity;
		this.position = 0;
	}

	public MemoryBlockV5(ByteBufferPool byteBufferPool, ByteBuffer memory) {
		this(byteBufferPool, memory, new ReferenceCount());
	}

	public MemoryBlockV5(ByteBufferPool byteBufferPool, ByteBuffer memory, ReferenceCount referenceCount) {
		this.nioBuffer = memory;
		this.array = memory.array();
		this.memoryPool = byteBufferPool;
		this.lock = new ReentrantLock();
		this.referenceCount = referenceCount;
	}

	public byte[] array() {
		return array;
	}

	public int capacity() {
		return capacity;
	}

	public ByteBuf clear() {
		this.position = 0;
		this.limit = capacity;
		return this;
	}

	public ByteBuf duplicate() {

		ReentrantLock lock = this.lock;

		lock.lock();

		try {

			if (released) {
				throw new ReleasedException("released");
			}

			MemoryBlockV5 block = new MemoryBlockV5(memoryPool, nioBuffer, referenceCount);

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
		this.limit = position;
		this.position = 0;
		return this;
	}
	
	private int ix(int index){
		return offset + index;
	}

	public byte get(int index) {
		return array[ix(index)];
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
		System.arraycopy(array, ix(position), dst, offset, length);
		this.position += length;
	}

	public int getInt() {
		int v = MathUtil.byte2Int(array, ix(position));
		this.position += 4;
		return v;
	}

	public int getInt(int index) {
		return MathUtil.byte2Int(array, ix(index));
	}

	public long getLong() {
		long v = MathUtil.byte2Long(array, ix(position));
		this.position += 8;
		return v;
	}

	public long getLong(int index) {
		return MathUtil.byte2Long(array, ix(index));
	}

	public boolean hasArray() {
		return true;
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

	public void put(byte[] src, int offset, int length) {
		System.arraycopy(src, offset, array, ix(position), length);;
		this.position += length;
	}
	
	public ByteBuffer nioBuffer(){
		return (ByteBuffer) nioBuffer.limit(ix(limit)).position(ix(position));
	}

	public int read(SocketChannel channel) throws IOException {

		int length = channel.read(nioBuffer());

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

			buffer.get(array, ix(position), remaining);

			this.position(this.limit);

			return remaining;
		} else {

			buffer.get(array, ix(position), srcRemaining);

			this.position(this.position + srcRemaining);

			return srcRemaining;
		}
	}

	public void setMemory(MemoryUnitV5 memoryStart, MemoryUnitV5 memoryEnd) {
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

	public int write(SocketChannel channel) throws IOException {

		int length = channel.write(nioBuffer());

		if (length > 0) {

			position += length;

			channel.upNetworkState();

		} else {

			channel.downNetworkState();
		}

		return length;
	}

	public byte get() {
		return array[ix(position++)];
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
		array[ix(position++)] = b;
	}

	public int read(ByteBuf buf) throws IOException {
		
		int srcRemaining = buf.remaining();

		if (srcRemaining == 0) {
			return 0;
		}

		int remaining = this.remaining();

		if (remaining <= srcRemaining) {

			buf.get(array, ix(position), remaining);

			position(limit);

			return remaining;
		} else {

			buf.get(array, ix(position), srcRemaining);

			position(position + srcRemaining);

			return srcRemaining;
		}
	}

}
