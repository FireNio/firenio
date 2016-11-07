package com.generallycloud.nio.buffer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.AbstractLifeCycle;

public abstract class AbstractMemoryPool extends AbstractLifeCycle implements ByteBufferPool {

	protected MemoryUnit[]	memoryUnits;

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

}
