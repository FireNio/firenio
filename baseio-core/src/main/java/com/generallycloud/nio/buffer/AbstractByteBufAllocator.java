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
public abstract class AbstractByteBufAllocator extends AbstractLifeCycle implements ByteBufAllocator {

	protected AbstractByteBuf[]	bufs;

	protected int				capacity;

	protected int				unitMemorySize;

	protected int				mask;

	protected ReentrantLock		lock		= new ReentrantLock();
	
	private Logger				logger	= LoggerFactory.getLogger(AbstractByteBufAllocator.class);

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

	protected void doStart() throws Exception {

		int capacity = this.capacity;
		
		initializeMemory(capacity * unitMemorySize);

		AbstractByteBuf[] bufs = new AbstractByteBuf[capacity];

		for (int i = 0; i < capacity; i++) {
			AbstractByteBuf buf = newByteBuf();
			buf.free = true;
			buf.index = i;
			bufs[i] = buf;
		}
		
		this.bufs = bufs;
	}

	protected abstract void initializeMemory(int capacity);

	protected void doStop() throws Exception {
		this.freeMemory();
	}

	public ByteBuf allocate(int capacity) {

		int size = (capacity + unitMemorySize - 1) / unitMemorySize;

		ReentrantLock lock = this.lock;

		lock.lock();

		try {

			ByteBuf buf = allocate(capacity, mask, this.capacity - size, size);

			if (buf == null) {

				buf = allocate(capacity, 0, mask - size, size);

				if (buf == null) {
					return UnpooledByteBufAllocator.allocate(capacity);
				}
			}

			busyBuf.put(buf, new RuntimeException(String.valueOf(capacity)));

			return buf;

		} finally {
			lock.unlock();
		}
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

				setEmploy(memoryStart, memoryEnd, blockEnd);

				mask = blockEnd;

				return memoryStart.produce(capacity);
			}

			start++;
		}

		return null;
	}

	private void setEmploy(AbstractByteBuf memoryStart, AbstractByteBuf memoryEnd, int blockEnd) {
		memoryStart.free = false;
		memoryStart.blockEnd = blockEnd;
		memoryEnd.free = false;
	}

	protected abstract AbstractByteBuf newByteBuf();

	private List<AbstractByteBuf>		busyUnit	= new ArrayList<AbstractByteBuf>();

	private Map<ByteBuf, Exception>	busyBuf	= new HashMap<ByteBuf, Exception>();

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
}
