package com.generallycloud.nio.buffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.component.TCPEndPoint;

// 注意，在对buffer写入数据时，没有做reference数量判断，
// 请勿对duplicate出来的buffer做写入操作
public class PooledByteBuffer implements ByteBuf {

	private ByteBuffer		memory;

	private boolean		released;

	// private ReferenceCount referenceCount;

	private ByteBufferPool	byteBufferPool	= null;

	protected PooledByteBuffer() {

	}

	public PooledByteBuffer(ByteBufferPool byteBufferPool, ByteBuffer memory) {
		// this.referenceCount = new ReferenceCount();
		this.byteBufferPool = byteBufferPool;
		this.memory = memory;
	}

	private ReentrantLock	lock	= new ReentrantLock();

	public void release() {

		ReentrantLock lock = this.lock;

		lock.lock();

		try {

			if (released) {
				throw new ReleasedException("released");
			}

			// if (referenceCount.deincreament() > 0) {
			// return;
			// }

			released = true;

			memory.clear();

			byteBufferPool.offer(this);

		} finally {
			lock.unlock();
		}
	}

	public ByteBuf duplicate() {

		ReentrantLock lock = this.lock;

		lock.lock();

		try {

			if (released) {
				throw new ReleasedException("released");
			}

			PooledByteBuffer byteBuffer = new PooledByteBuffer();

			byteBuffer.memory = memory.duplicate();
			byteBuffer.byteBufferPool = byteBufferPool;
			// byteBuffer.referenceCount = referenceCount;
			// byteBuffer.referenceCount.increament();

			return byteBuffer;

		} finally {
			lock.unlock();
		}
	}
	
	public void flip() {
		memory.flip();
	}

	public void clear() {
		memory.clear();
	}

	public int getInt() {
		return memory.getInt();
	}

	public int getInt(int offset) {
		return memory.getInt(offset);
	}

	public long getLong(int offset) {
		return memory.getLong(offset);
	}

	public long getLong() {
		return memory.getLong();
	}

	public byte[] array() {
		return memory.array();
	}

	public void getBytes(byte[] dst) {
		getBytes(dst, 0, dst.length);
	}

	public void getBytes(byte[] dst, int offset, int length) {
		memory.get(dst, offset, length);
	}

	public void putBytes(byte[] src) {
		getBytes(src, 0, src.length);
	}

	public void putBytes(byte[] src, int offset, int length) {
		memory.put(src, offset, length);
	}

	public int remaining() {
		return memory.remaining();
	}

	public int read(TCPEndPoint endPoint) throws IOException {
		return endPoint.read(memory);
	}

	public int write(TCPEndPoint endPoint) throws IOException {
		return endPoint.write(memory);
	}

	public int position() {
		return memory.position();
	}

	public int limit() {
		return memory.limit();
	}

	public int capacity() {
		return memory.capacity();
	}

	public boolean hasRemaining() {
		return memory.hasRemaining();
	}

	public void position(int position) {
		memory.position(position);
	}

	public void limit(int limit) {
		memory.limit(limit);
	}
	
	public void touch() {
		this.released = false;
	}

	public boolean hasArray() {
		return memory.hasArray();
	}
	
	public String toString() {
		return memory.toString();
	}
	
}
