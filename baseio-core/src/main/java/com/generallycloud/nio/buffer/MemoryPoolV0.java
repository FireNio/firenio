package com.generallycloud.nio.buffer;

import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.common.ReleaseUtil;

@Deprecated
public abstract class MemoryPoolV0 extends AbstractMemoryPool {

	protected int						size		= 0;
	protected ArrayBlockingQueue<ByteBuf>	buffers	= null;
	protected ReentrantLock				lock		= new ReentrantLock();

	public MemoryPoolV0(int capacity) {
		super(capacity);
		this.buffers = new ArrayBlockingQueue<ByteBuf>(capacity);
	}

	protected void doStart() throws Exception {

	}

	private ByteBuf newByteBuffer() {

		ReentrantLock lock = this.lock;

		lock.lock();

		try {

			if (size > capacity) {
				return null;
			}

			size++;

			ByteBuffer memory = allocateMemory(ByteBuf.UNIT_CAPACITY);

			return new PooledByteBuffer(this, memory);

		} finally {

			lock.unlock();
		}
	}

	public void release(ByteBuf buffer) {
		if (!buffers.offer(buffer)) {
			throw new RuntimeException("system error");
		}
	}

	private ByteBuf pollElement() {

		ByteBuf buf = buffers.poll();

		if (buf == null) {
			buf = newByteBuffer();
		}

		return buf;
	}

	public ByteBuf allocate(int capacity) {

		int bufsSize = (capacity + ByteBuf.UNIT_CAPACITY - 1) / ByteBuf.UNIT_CAPACITY;

		ByteBuf[] bufs = new ByteBuf[bufsSize];

		for (int i = 0; i < bufs.length; i++) {

			ByteBuf buf = pollElement();

			if (buf == null) {

				for (int j = 0; j < i; j++) {
					ReleaseUtil.release(bufs[j]);
				}

				throw new BufferException("no enough buf");
			}

			// buf.touch();

			bufs[i] = buf;
		}

		return new PooledByteBufferGroup(bufs, bufsSize * ByteBuf.UNIT_CAPACITY, capacity);
	}

}
