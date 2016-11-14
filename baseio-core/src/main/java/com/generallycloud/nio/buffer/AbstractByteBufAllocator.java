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

	protected ByteBufUnit[]	memoryUnits;

	protected int			capacity;

	protected ReentrantLock	lock	= new ReentrantLock();

	protected int			unitMemorySize;

	protected int			mask;
	
	private Logger logger = LoggerFactory.getLogger(AbstractByteBufAllocator.class);

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

			// logger.info("release : {}",memoryBlock.capacity());

			ByteBufUnit memoryStart = _buf.memoryStart;
			ByteBufUnit memoryEnd = _buf.memoryEnd;

			// logger.debug("setFree,start={},end={}", memoryStart.index,
			// memoryEnd.index );
			// new Exception().printStackTrace();

			memoryStart.free = true;
//			memoryStart.blockEnd = -1;//FIXME 考虑不去重置
			memoryEnd.free = true;
			
			busyBuf.remove(buf);

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
			
			busyBuf.put(buf, new RuntimeException(String.valueOf(capacity)));
			
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
	
	private Map<ByteBuf, Exception> busyBuf = new HashMap<ByteBuf, Exception>();

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
		
		printBusy();

		return b.toString();
	}
	
	private void printBusy(){
		
		if (busyBuf.size() == 0) {
			
			return;
		}
		
		ReentrantLock lock = this.lock;
		
		lock.lock();
		
		try{

			Set<Entry<ByteBuf, Exception>> es = busyBuf.entrySet();
			
			for(Entry<ByteBuf, Exception> e :es){
				
				Exception ex = e.getValue();
				
				logger.error(ex.getMessage(),ex);
			}
			
		}finally{
			
			lock.unlock();
		}
		
	}
}
