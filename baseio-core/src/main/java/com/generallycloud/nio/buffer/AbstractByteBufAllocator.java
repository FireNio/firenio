package com.generallycloud.nio.buffer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.AbstractLifeCycle;

public abstract class AbstractByteBufAllocator extends AbstractLifeCycle implements ByteBufAllocator {

	protected ByteBufUnit[]	memoryUnits;

	protected int			capacity;

	protected ReentrantLock	lock	= new ReentrantLock();

	protected int			unitMemorySize;

	protected int			mask;

	public AbstractByteBufAllocator(int capacity) {
		this(capacity, 1024);
	}

	public AbstractByteBufAllocator(int capacity, int unitMemorySize) {
		this.capacity = capacity;
		this.unitMemorySize = unitMemorySize;
	}

	public int getUnitMemorySize() {
		return unitMemorySize;
	}

	public int getCapacity() {
		return capacity;
	}
	
	public void release(ByteBuf memoryBlock) {

		AbstractByteBuf block = (AbstractByteBuf) memoryBlock;

		ReentrantLock lock = this.lock;

		lock.lock();

		try {

			// logger.info("release : {}",memoryBlock.capacity());

			ByteBufUnit memoryStart = block.memoryStart;
			ByteBufUnit memoryEnd = block.memoryEnd;

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
	
	protected void doStart() throws Exception {

		int capacity = this.capacity;

		this.memoryUnits = new ByteBufUnit[capacity];

		for (int i = 0; i < capacity; i++) {
			ByteBufUnit unit = new ByteBufUnit();
			unit.free = true;
			unit.index = i;
			memoryUnits[i] = unit;
		}
		
		initializeMemory(capacity * unitMemorySize);
	}

	protected abstract void initializeMemory(int capacity);
	
	protected void doStop() throws Exception {
		this.freeMemory();
	}
	
	public ByteBuf allocate(int capacity) {

		int size = (capacity + unitMemorySize - 1) / unitMemorySize;

		ReentrantLock lock = this.lock;

		lock.lock();

		// logger.info("allocate : {}",capacity);

		try {

			ByteBuf buf = allocate(capacity, mask, this.capacity - size, size);

			if (buf == null) {
				
				buf = allocate(capacity, 0, mask - size, size);
				
				if (buf == null) {
					return UnpooledByteBufAllocator.allocate(capacity);
				}
			}
			
			return buf;

		} finally {
			lock.unlock();
		}
	}

	private ByteBuf allocate(int capacity, int start, int end, int size) {

		ByteBufUnit[] memoryUnits = this.memoryUnits;

		int freeSize = 0;

		for (; start < end;) {

			ByteBufUnit unit = memoryUnits[start];

			if (!unit.free) {

				start = unit.blockEnd;

				freeSize = 0;

				continue;
			}

			if (++freeSize == size) {

				int blockEnd = unit.index + 1;
				start = blockEnd - size;

				ByteBufUnit memoryStart = memoryUnits[start];
				ByteBufUnit memoryEnd = unit;

				setEmploy(memoryStart, memoryEnd, blockEnd);

				AbstractByteBuf byteBuf = newByteBuf();

				byteBuf.setMemory(memoryStart, memoryEnd);

				mask = blockEnd;

				return byteBuf.produce(capacity);
			}

			start++;
		}

		return null;
	}
	
	private void setEmploy(ByteBufUnit memoryStart, ByteBufUnit memoryEnd, int blockEnd) {
		// logger.debug("setEmploy,start={},end={}", memoryStart.index,
		// memoryEnd.index);
		// new Exception().printStackTrace();

		memoryStart.free = false;
		memoryStart.blockEnd = blockEnd;
		memoryEnd.free = false;
	}
	
	protected abstract AbstractByteBuf newByteBuf();

	private List<ByteBufUnit>	busyUnit	= new ArrayList<ByteBufUnit>();

	public String toString() {

		busyUnit.clear();

		ByteBufUnit[] memoryUnits = this.memoryUnits;

		int free = 0;

		for (ByteBufUnit b : memoryUnits) {

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

}
