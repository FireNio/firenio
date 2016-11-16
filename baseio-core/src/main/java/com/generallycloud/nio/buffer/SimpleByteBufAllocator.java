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

//FIXME 考虑每个selector持有一个allocator，失败时从下一个selector获取
public class SimpleByteBufAllocator extends AbstractLifeCycle implements ByteBufAllocator {

	private AbstractByteBuf[]		bufs;

	private int					capacity;

	private int					mask;

	private int					unitMemorySize;

	private ReentrantLock			lock		= new ReentrantLock();

	private Map<ByteBuf, Exception>	busyBuf	= new HashMap<ByteBuf, Exception>();

	private List<AbstractByteBuf>		busyUnit	= new ArrayList<AbstractByteBuf>();

	private Logger					logger	= LoggerFactory.getLogger(SimpleByteBufAllocator.class);

	private ByteBufFactory			bufFactory;

	private boolean				isDirect;

	public SimpleByteBufAllocator(int capacity, int unitMemorySize, boolean isDirect) {
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

	private ByteBuf allocate(int capacity, int start, int end, int size) {

		AbstractByteBuf[] memoryUnits = this.bufs;

		int freeSize = 0;

		for (; start < end;) {

			AbstractByteBuf unit = memoryUnits[start];

			if (!unit.free) {

				start = unit.blockEnd;

				freeSize = 0;

				continue;
			}

			if (++freeSize == size) {

				int blockEnd = unit.index + 1;
				start = blockEnd - size;

				AbstractByteBuf memoryStart = memoryUnits[start];
				AbstractByteBuf memoryEnd = unit;

				memoryStart.free = false;
				memoryStart.blockEnd = blockEnd;
				memoryEnd.free = false;

				mask = blockEnd;

				return memoryStart.produce(capacity);
			}

			start++;
		}

		return null;
	}

	protected void doStart() throws Exception {

		bufFactory = createBufFactory();

		int capacity = this.capacity;

		initializeMemory(capacity * unitMemorySize);

		AbstractByteBuf[] bufs = new AbstractByteBuf[capacity];

		for (int i = 0; i < capacity; i++) {
			AbstractByteBuf buf = bufFactory.newByteBuf(this);
			buf.free = true;
			buf.index = i;
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

		AbstractByteBuf _buf = (AbstractByteBuf) buf;

		ReentrantLock lock = this.lock;

		lock.lock();

		try {

			AbstractByteBuf memoryStart = _buf;
			AbstractByteBuf memoryEnd = bufs[memoryStart.blockEnd - 1];

			memoryStart.free = true;
			memoryEnd.free = true;

			busyBuf.remove(buf);

		} finally {
			lock.unlock();
		}
	}

	public String toString() {

		busyUnit.clear();

		AbstractByteBuf[] memoryUnits = this.bufs;

		int free = 0;

		for (AbstractByteBuf b : memoryUnits) {

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

		printBusy();

		return b.toString();
	}
}
