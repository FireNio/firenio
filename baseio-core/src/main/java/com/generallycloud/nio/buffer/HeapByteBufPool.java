package com.generallycloud.nio.buffer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class HeapByteBufPool extends AbstractMemoryPool {

	// private Logger logger = LoggerFactory.getLogger(MemoryPoolV3.class);

	private MemoryUnit[]	memoryUnits;

	private int			mask;

	protected byte[]		memory;

	protected void doStart() throws Exception {

		int capacity = this.capacity;

		this.memory = new byte[capacity * unitMemorySize];

		this.memoryUnits = new MemoryUnit[capacity];

		for (int i = 0; i < capacity; i++) {
			MemoryUnit unit = new MemoryUnit();
			unit.free = true;
			unit.index = i;
			memoryUnits[i] = unit;
		}
	}

	protected void setEmploy(MemoryUnit memoryStart, MemoryUnit memoryEnd, int blockEnd) {
		// logger.debug("setEmploy,start={},end={}", memoryStart.index,
		// memoryEnd.index);
		// new Exception().printStackTrace();

		memoryStart.free = false;
		memoryStart.blockEnd = blockEnd;
		memoryEnd.free = false;
	}

	public ByteBuf allocate(int capacity) {

		int size = (capacity + unitMemorySize - 1) / unitMemorySize;

		ReentrantLock lock = this.lock;

		lock.lock();

		// logger.info("allocate : {}",capacity);

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

		MemoryUnit[] memoryUnits = this.memoryUnits;

		int freeSize = 0;

		for (; start < end;) {

			MemoryUnit unit = memoryUnits[start];

			if (!unit.free) {

				start = unit.blockEnd;

				freeSize = 0;

				continue;
			}

			if (++freeSize == size) {

				int blockEnd = unit.index + 1;
				start = blockEnd - size;

				MemoryUnit memoryStart = memoryUnits[start];
				MemoryUnit memoryEnd = unit;

				setEmploy(memoryStart, memoryEnd, blockEnd);

				HeapByteBuf memoryBlock = new HeapByteBuf(this, memory);

				memoryBlock.setMemory(memoryStart, memoryEnd);

				mask = blockEnd;

				return memoryBlock.produce(capacity);
			}

			start++;
		}

		return null;
	}

	public HeapByteBufPool(int capacity) {
		this(capacity, 1024);
	}

	public HeapByteBufPool(int capacity, int unitMemorySize) {
		super(capacity, unitMemorySize);
	}

	public void release(ByteBuf memoryBlock) {

		AbstractByteBuf block = (AbstractByteBuf) memoryBlock;

		ReentrantLock lock = this.lock;

		lock.lock();

		try {

			// logger.info("release : {}",memoryBlock.capacity());

			MemoryUnit memoryStart = block.memoryStart;
			MemoryUnit memoryEnd = block.memoryEnd;

			// logger.debug("setFree,start={},end={}", memoryStart.index,
			// memoryEnd.index );
			// new Exception().printStackTrace();

			memoryStart.free = true;
			memoryStart.blockEnd = -1;
			memoryEnd.free = true;

		} finally {
			lock.unlock();
		}
	}

	private List<MemoryUnit>	busyUnit	= new ArrayList<MemoryUnit>();

	public String toString() {

		busyUnit.clear();

		MemoryUnit[] memoryUnits = this.memoryUnits;

		int free = 0;

		for (MemoryUnit b : memoryUnits) {

			if (b.free) {
				free++;
			} else {
				busyUnit.add(b);
			}
		}

		StringBuilder b = new StringBuilder();
		b.append(this.getClass().getSimpleName());
		b.append("[free=");
		b.append(free);
		b.append(",memory=");
		b.append(capacity);
		b.append("]");

		return b.toString();
	}

	public void freeMemory() {
		this.memory = null;
	}
	
	

}
