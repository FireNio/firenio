package com.generallycloud.nio.buffer;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

@Deprecated
public abstract class MemoryPoolV2 extends AbstractMemoryPool {

//	private Logger			logger	= LoggerFactory.getLogger(MemoryPoolV2.class);

	private boolean[]		memoryUnits;

	private int			mask;

	protected ByteBuffer	memory;

	protected void doStart() throws Exception {

		int capacity = this.capacity;

		this.memory = allocateMemory(capacity * unitMemorySize);

		this.memoryUnits = new boolean[capacity];

		for (int i = 0; i < capacity; i++) {
			memoryUnits[i] = true;
		}
	}

	public void setFree(int start, int end, boolean free) {
//		logger.debug("setFree,start={},end={},free={}", new Object[] { start, end, free });
//		 new Exception().printStackTrace();
		boolean[] memoryUnits = this.memoryUnits;
		for (int i = start; i < end; i++) {
			memoryUnits[i] = free;
		}
	}

	public ByteBuf allocate(int capacity) {

		int size = (capacity + unitMemorySize - 1) / unitMemorySize;

		ReentrantLock lock = this.lock;

		lock.lock();

		try {

			ByteBuf buf = allocate(capacity,mask, this.capacity - size, size);

			if (buf == null) {

				return allocate(capacity,0, mask - size, size);
			}

			return buf;

		} finally {
			lock.unlock();
		}
	}

	private ByteBuf allocate(int capacity,int start, int end, int size) {

		boolean[] memoryUnits = this.memoryUnits;

		for (; start < end;) {

			if (!memoryUnits[start]) {
				start++;
				continue;
			}

			int _end = size + start;

			if (memoryUnits[_end]) {

				setFree(start, _end, false);

				MemoryBlockV2 memoryBlock = new MemoryBlockV2(this, memory.duplicate());

				memoryBlock.setMemory(start, _end);

				mask = _end;

				return memoryBlock.use().limit(capacity);
			}

			start = _end + 1;
		}

		return null;
	}

	public MemoryPoolV2(int capacity) {
		this(capacity, 1024);
	}

	public MemoryPoolV2(int capacity, int unitMemorySize) {
		super(capacity, unitMemorySize);
	}

	public void release(ByteBuf memoryBlock) {

		MemoryBlockV2 block = (MemoryBlockV2) memoryBlock;

		ReentrantLock lock = this.lock;

		lock.lock();

		try {

			this.setFree(block.getStart(), block.getEnd(), true);

		} finally {
			lock.unlock();
		}
	}

	public String toString() {

		boolean[] memoryUnits = this.memoryUnits;

		int free = 0;

		for (boolean b : memoryUnits) {

			if (b) {
				free++;
			}
		}

		StringBuilder b = new StringBuilder();
		b.append(this.getClass().getName());
		b.append("[free=");
		b.append(free);
		b.append(",memory=");
		b.append(capacity);
		b.append("]");

		return b.toString();
	}
	
	public String toSimpleString() {

		boolean[] memoryUnits = this.memoryUnits;

		int free = 0;

		for (boolean b : memoryUnits) {

			if (b) {
				free++;
			}
		}

		StringBuilder b = new StringBuilder();
		b.append("[free=");
		b.append(free);
		b.append(",memory=");
		b.append(capacity);
		b.append("]");

		return b.toString();
	}

}
