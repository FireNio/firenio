package com.generallycloud.nio.buffer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.AbstractLifeCycle;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;

public abstract class AbstractByteBufAllocator extends AbstractLifeCycle implements ByteBufAllocator {

	protected PooledByteBuf[]		bufs;

	protected int					capacity;

	protected int					mask;
	
	protected boolean				isDirect;

	protected int					unitMemorySize;
	
	protected ByteBufFactory		bufFactory	;

	protected ReentrantLock			lock			= new ReentrantLock();

	protected Map<ByteBuf, Exception>	busyBuf		= new HashMap<ByteBuf, Exception>();

	protected List<PooledByteBuf>	busyUnit		= new ArrayList<PooledByteBuf>();

	protected Logger				logger		= LoggerFactory.getLogger(AbstractByteBufAllocator.class);

	public AbstractByteBufAllocator(int capacity, int unitMemorySize, boolean isDirect) {
		this.isDirect = isDirect;
		this.capacity = capacity;
		this.unitMemorySize = unitMemorySize;
	}

	public boolean isDirect() {
		return isDirect;
	}

	public ByteBuf allocate(int capacity) {

		int size = (capacity + unitMemorySize - 1) / unitMemorySize;

		ReentrantLock lock = this.lock;

		lock.lock();

		try {

			ByteBuf buf = allocate(capacity, mask, this.capacity - size, size);

			if (buf == null) {

				buf = allocate(capacity, 0, mask - size, size);
			}

			busyBuf.put(buf, new RuntimeException(String.valueOf(capacity)));

			return buf;

		} finally {
			lock.unlock();
		}
	}

	public void freeMemory() {
		bufFactory.freeMemory();
	}

	protected abstract ByteBuf allocate(int capacity, int start, int end, int size) ;

	protected void doStart() throws Exception {

		bufFactory = createBufFactory();

		int capacity = this.capacity;

		initializeMemory(capacity * unitMemorySize);

		PooledByteBuf[] bufs = new PooledByteBuf[capacity];

		for (int i = 0; i < capacity; i++) {
			PooledByteBuf buf = bufFactory.newByteBuf(this);
			buf.setFree(true);
			buf.setIndex(i);
			bufs[i] = buf;
		}

		this.bufs = bufs;
	}

	private ByteBufFactory createBufFactory() {
		if (isDirect) {
			return new DirectByteBufFactory();
		}
		return new HeapByteBufFactory();
	}

	protected void doStop() throws Exception {
		this.freeMemory();
	}

	public int getCapacity() {
		return capacity;
	}

	public int getUnitMemorySize() {
		return unitMemorySize;
	}

	protected void initializeMemory(int capacity) {
		bufFactory.initializeMemory(capacity);
	}

	private void printBusy() {

		if (busyBuf.size() == 0) {

			return;
		}

		ReentrantLock lock = this.lock;

		lock.lock();

		try {

			Set<Entry<ByteBuf, Exception>> es = busyBuf.entrySet();

			for (Entry<ByteBuf, Exception> e : es) {

				Exception ex = e.getValue();

				logger.error(ex.getMessage(), ex);
			}

		} finally {

			lock.unlock();
		}
	}

	public void release(ByteBuf buf) {

		ReentrantLock lock = this.lock;

		lock.lock();

		try {

			doRelease((PooledByteBuf) buf);

			busyBuf.remove(buf);

		} finally {
			lock.unlock();
		}
	}
	
	protected abstract void doRelease(PooledByteBuf buf);

	public String toString() {

		busyUnit.clear();

		PooledByteBuf[] memoryUnits = this.bufs;

		int free = 0;

		for (PooledByteBuf b : memoryUnits) {

			if (b.isFree()) {
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

		printBusy();

		return b.toString();
	}
}
