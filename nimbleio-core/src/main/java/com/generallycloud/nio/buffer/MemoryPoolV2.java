package com.generallycloud.nio.buffer;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;

public abstract class MemoryPoolV2 extends AbstractMemoryPool{
	
	private Logger logger = LoggerFactory.getLogger(MemoryPoolV2.class);

	private boolean[]		memoryUnits;

	protected ByteBuffer	memory;

	private boolean		ordinal = true;

	private int			middleIndex;

	protected void doStart() throws Exception {

		int capacity = this.capacity;

		this.middleIndex = this.capacity / 2;

		this.memory = allocateMemory(capacity * unitMemorySize);

		this.memoryUnits = new boolean[capacity];

		for (int i = 0; i < capacity; i++) {
			memoryUnits[i] = true;
		}
	}

	public void setFree(int start, int end, boolean free) {
		logger.debug("setFree,start={},end={},free={}",new Object[]{start,end,free});
//		new Exception().printStackTrace();
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

			boolean[] memoryUnits = this.memoryUnits;

			int _size = this.capacity - size;

			int i = 0;

//			if (ordinal) {
//				ordinal = false;
//			} else {
//				i = middleIndex;
//				ordinal = true;
//			}

			for (; i < _size;) {

				if (!memoryUnits[i]) {
					i++;
					continue;
				}

				int end = size + i;

				if (memoryUnits[end]) {

					setFree(i, end, false);

					MemoryBlockV2 memoryBlock = new MemoryBlockV2(this, memory);

					memoryBlock.setMemory(i, end);

					return memoryBlock.use().limit(capacity);
				}

				i = end + 1;
			}

			return null;

		} finally {
			lock.unlock();
		}
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
		
		for (boolean b :memoryUnits) {
			
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

}
