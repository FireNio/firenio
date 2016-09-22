package com.generallycloud.nio.buffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.component.SocketChannel;

@Deprecated
public class PooledByteBufferGroup implements ByteBuf {

	private ByteBuf[]		bufs;

	private ByteBuf		currentBuf;

	private ByteBuf		tailBuf;
	
	private ByteBuf		headBuf;

	private int			capacity;

	private int			position;

	private int			limit;

	private boolean		released;

	private ReferenceCount	referenceCount;
	
	private int bufIndex = 0;
	
	public byte[] getBytes() {
		return null;
	}

	private ReentrantLock	lock	= new ReentrantLock();

	public PooledByteBufferGroup(ByteBuf[] bufs, int capacity, int limit) {
		this.bufs = bufs;
		this.capacity = capacity;
		this.limit = limit;
		this.headBuf = bufs[0];
		this.currentBuf = headBuf;
		this.tailBuf = bufs[bufs.length - 1];
		this.referenceCount = new ReferenceCount();
		initBuf(tailBuf, limit);
	}

	private void initBuf(ByteBuf lastBuf, int limit) {

		int _limit = limit % lastBuf.capacity();

		lastBuf.limit(_limit);
	}

	PooledByteBufferGroup() {
	}
	
	public int offset() {
		return 0;
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

			for (ByteBuf buf : bufs) {
				ReleaseUtil.release(buf);
			}

		} finally {
			lock.unlock();
		}
	}

	public int read(SocketChannel channel) throws IOException {
		
		ByteBuf buf = findBuf();
		
		int read = buf.read(channel);
		
		position += read;
		
		return read;
	}
	
	public ByteBuffer getMemory() {
		return null;
	}

	private ByteBuf findBuf() {
		
		ByteBuf buf = currentBuf;
		
		if (!buf.hasRemaining()) {
			
			bufIndex++;
			
			if (bufIndex < bufs.length) {
				
				buf = currentBuf = bufs[bufIndex];
				
			}else{
				
				throw new BufferException("no buf available");
			}
		}
		return buf;
	}

	public int write(SocketChannel channel) throws IOException {
		
		ByteBuf buf = findBuf();
		
		int read = buf.write(channel);
		
		position += read;
		
		return read;
	}

	public void get(byte[] dst) {
		get(dst, 0, dst.length);
	}

	public void get(byte[] dst, int offset, int length) {
		
		if (offset != 0) {
			throw new UnsupportedOperationException();
		}
		
		int unit_capacity = ByteBuf.UNIT_CAPACITY;
		
		if (length < unit_capacity) {
			
			headBuf.get(dst, offset, length);
			return;
		}
		
		int size = length / unit_capacity;
		
		for (int i = 0; i < size; i++) {
			
			bufs[i].get(dst, unit_capacity * i, unit_capacity);
		}
		
		int remain = length % unit_capacity;
		
		if (remain > 0) {
			bufs[size].get(dst, size * unit_capacity, remain);
		}
	}

	public void put(byte[] src) {
		put(src, 0, src.length);
	}

	public void put(byte[] src, int offset, int length) {
		
		if (offset != 0) {
			throw new UnsupportedOperationException();
		}
		
		int unit_capacity = ByteBuf.UNIT_CAPACITY;
		
		if (length < unit_capacity) {
			
			headBuf.put(src, offset, length);
			return;
		}
		
		int size = length / unit_capacity;
		
		for (int i = 0; i < size; i++) {
			
			bufs[i].put(src, unit_capacity * i, unit_capacity);
		}
		
		int remain = length % unit_capacity;
		
		if (remain > 0) {
			bufs[size+1].put(src, size * unit_capacity, remain);
		}
	}
	
	public ByteBuf flip(){
		limit = position;
		position = 0;
		
		int size = (limit + ByteBuf.UNIT_CAPACITY - 1) / ByteBuf.UNIT_CAPACITY;
		
		for (int i = 0; i < size; i++) {
			bufs[i].flip();
		}
		
		return this;
	}

	public ByteBuf duplicate() {

		ReentrantLock lock = this.lock;

		lock.lock();

		try {

			if (released) {
				throw new ReleasedException("released");
			}

			PooledByteBufferGroup group = new PooledByteBufferGroup();

			group.bufs = copyBufs();
			group.referenceCount = referenceCount;
			group.referenceCount.increament();

			return group;

		} finally {
			lock.unlock();
		}
	}

	private ByteBuf[] copyBufs() {

		ByteBuf[] bufs = this.bufs;

		ByteBuf[] copy = new ByteBuf[this.bufs.length];

		for (int i = 0; i < copy.length; i++) {
			copy[i] = bufs[i].duplicate();
		}

		return copy;
	}

	public int remaining() {
		return limit - position;
	}

	public int position() {
		return position;
	}

	public ByteBuf position(int position) {
		this.position = position;
		return this;
	}

	public int limit() {
		return limit;
	}

	public ByteBuf limit(int limit) {
		
		this.limit = limit;
		
		int _limit = limit % ByteBuf.UNIT_CAPACITY;
		
		if (_limit == 0) {
			_limit = ByteBuf.UNIT_CAPACITY;
		}
		
		tailBuf.limit(_limit);
		
		return this;
	}

	public int capacity() {
		return capacity;
	}

	public boolean hasRemaining() {
		return remaining() > 0;
	}

	public boolean hasArray() {

		if (bufs.length == 1) {
			return bufs[0].hasArray();
		}

		return false;
	}

	public ByteBuf clear() {
		limit = capacity;
		position = 0;
		for (ByteBuf buf : bufs) {
			buf.clear();
		}
		return this;
	}
	
	public void touch() {
		
	}

	public int getInt() {
		return currentBuf.getInt();
	}

	public long getLong() {
		return currentBuf.getLong();
	}
	
	private ByteBuf findBuf(int offset){
		int bufIndex = offset / ByteBuf.UNIT_CAPACITY;
		return bufs[bufIndex];
	}

	public int getInt(int offset) {
		return findBuf(offset).getInt(offset % ByteBuf.UNIT_CAPACITY);
	}

	public long getLong(int offset) {
		return findBuf(offset).getLong(offset % ByteBuf.UNIT_CAPACITY);
	}

	public byte[] array() {
		return bufs[0].array();
	}

	public byte get(int index) {
		return 0;
	}
	
}
