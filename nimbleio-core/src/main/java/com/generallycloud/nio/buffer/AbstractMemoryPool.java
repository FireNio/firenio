package com.generallycloud.nio.buffer;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.AbstractLifeCycle;

public abstract class AbstractMemoryPool extends AbstractLifeCycle implements ByteBufferPool {

	protected int			capacity;

	protected ReentrantLock	lock	= new ReentrantLock();

	protected int			unitMemorySize;
	
	public AbstractMemoryPool(int capacity) {
		this(capacity, 1024);
	}

	public AbstractMemoryPool(int capacity, int unitMemorySize) {
		this.capacity = capacity;
		this.unitMemorySize = unitMemorySize;
	}

	public int getUnitMemorySize() {
		return unitMemorySize;
	}

	public int getCapacity() {
		return capacity;
	}

	protected void doStop() throws Exception {
		this.freeMemory();
	}
	
	protected abstract ByteBuffer allocateMemory(int capacity);
	
}
