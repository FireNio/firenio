package com.generallycloud.nio.buffer;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

public abstract class MemoryPoolV3 extends AbstractMemoryPool {

//	private Logger			logger	= LoggerFactory.getLogger(MemoryPoolV3.class);

	private MemoryUnitV3[]	memoryUnits;

	private int			mask;

	protected ByteBuffer	memory;

	protected void doStart() throws Exception {

		int capacity = this.capacity;

		this.memory = allocateMemory(capacity * unitMemorySize);

		this.memoryUnits = new MemoryUnitV3[capacity];

		for (int i = 0; i < capacity; i++) {
			MemoryUnitV3 unit = new MemoryUnitV3();
			unit.free = true;
			unit.index = i;
			memoryUnits[i] = unit;
		}
	}

	public void setEmploy(MemoryUnitV3 memoryStart, MemoryUnitV3 memoryEnd,int blockEnd) {
//		logger.debug("setEmploy,start={},end={}", memoryStart.index, memoryEnd.index);
		// new Exception().printStackTrace();

		memoryStart.free = false;
		memoryStart.blockEnd = blockEnd;
		memoryEnd.free = false;
	}

	public ByteBuf allocate(int capacity) {

		int size = (capacity + unitMemorySize - 1) / unitMemorySize;

		ReentrantLock lock = this.lock;

		lock.lock();

		try {

			ByteBuf buf = allocate(capacity, mask, this.capacity - size, size);

			if (buf == null) {

				return allocate(capacity, 0, mask - size, size);
			}

			return buf;

		} finally {
			lock.unlock();
		}
	}

	private ByteBuf allocate(int capacity, int start, int end, int size) {

		MemoryUnitV3[] memoryUnits = this.memoryUnits;

		int freeSize = 0;

		for (; start < end;) {

			MemoryUnitV3 unit = memoryUnits[start];

			if (!unit.free) {

				start = unit.blockEnd;

				continue;
			}

			if (++freeSize == size) {

				int blockEnd = unit.index + 1;
				start = blockEnd - size;

				MemoryUnitV3 memoryStart = memoryUnits[start];
				MemoryUnitV3 memoryEnd = unit;
				
				setEmploy(memoryStart, memoryEnd,blockEnd);

				MemoryBlockV3 memoryBlock = new MemoryBlockV3(this, memory.duplicate());

				memoryBlock.setMemory(memoryStart, memoryEnd);

				mask = blockEnd;

				return memoryBlock.use().limit(capacity);
			}

			start++;
		}

		return null;
	}

	public MemoryPoolV3(int capacity) {
		this(capacity, 1024);
	}

	public MemoryPoolV3(int capacity, int unitMemorySize) {
		super(capacity, unitMemorySize);
	}

	public void release(ByteBuf memoryBlock) {

		MemoryBlockV3 block = (MemoryBlockV3) memoryBlock;

		ReentrantLock lock = this.lock;

		lock.lock();

		try {

			MemoryUnitV3 memoryStart = block.memoryStart;
			MemoryUnitV3 memoryEnd = block.memoryEnd;

//			logger.debug("setFree,start={},end={}", memoryStart.index, memoryEnd.index );
			// new Exception().printStackTrace();

			memoryStart.free = true;
			memoryStart.blockEnd = -1;
			memoryEnd.free = true;

		} finally {
			lock.unlock();
		}
	}

	public String toString() {

		MemoryUnitV3[] memoryUnits = this.memoryUnits;

		int free = 0;

		for (MemoryUnitV3 b : memoryUnits) {

			if (b.free) {
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

		MemoryUnitV3[] memoryUnits = this.memoryUnits;

		int free = 0;

		for (MemoryUnitV3 b : memoryUnits) {

			if (b.free) {
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
