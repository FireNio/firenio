package com.generallycloud.nio.buffer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.AbstractLifeCycle;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.ReleaseUtil;

public abstract class AbstractByteBufAllocator extends AbstractLifeCycle implements ByteBufAllocator {

	protected ByteBufUnit[]			units;

	protected int					capacity;

	protected int					mask;

	protected boolean				isDirect;

	protected int					unitMemorySize;

	protected ByteBufFactory		bufFactory;

	protected ReentrantLock			lock		;

	protected List<ByteBufUnit>		busyUnit	= new ArrayList<ByteBufUnit>();

	protected Logger				logger	= LoggerFactory.getLogger(AbstractByteBufAllocator.class);

	public AbstractByteBufAllocator(int capacity, int unitMemorySize, boolean isDirect) {
		this.isDirect = isDirect;
		this.capacity = capacity;
		this.unitMemorySize = unitMemorySize;
	}

	@Override
	public boolean isDirect() {
		return isDirect;
	}

	@Override
	public ByteBuf allocate(int limit) {
		return allocate(bufFactory, limit);
	}
	
	private PooledByteBuf allocate(ByteBufNew byteBufNew,int limit) {
		
		int size = (limit + unitMemorySize - 1) / unitMemorySize;

		ReentrantLock lock = this.lock;

		lock.lock();
		
		try {

			int mask = this.mask;
			
			PooledByteBuf buf = allocate(byteBufNew,limit, mask, this.capacity, size);

			if (buf == null) {

				buf = allocate(byteBufNew,limit, 0, mask, size);
			}

			return buf;

		} finally {
			lock.unlock();
		}
	}
	
	@Override
	public void reallocate(ByteBuf buf, int limit, boolean copyOld) {
		
		if (copyOld) {
			
			if (limit > buf.capacity()) {
				
				PooledByteBuf newBuf = allocate(bufFactory,limit);
				
				newBuf.read(buf.position(0));
				
				ReleaseUtil.release(buf);
				
				buf.newByteBuf(this).produce(newBuf);
				
				return;
			}
			
			int oldLimit = buf.limit();
			
			buf.limit(limit).skipBytes(oldLimit);
			
			return;
		}
		
		if (limit > buf.capacity()) {
			
			ReleaseUtil.release(buf);
			
			allocate(buf, limit);
			
			return;
		}
		
		buf.limit(limit);
		
	}

	@Override
	public void reallocate(ByteBuf buf, int limit, int maxLimit, boolean copyOld) {
		if (limit > maxLimit) {
			throw new BufferException("limit:" + limit +",maxLimit:"+maxLimit);
		}
		reallocate(buf,limit,copyOld);
	}

	@Override
	public void reallocate(ByteBuf buf, int limit) {
		reallocate(buf, limit, false);
	}

	@Override
	public void reallocate(ByteBuf buf, int limit, int maxLimit) {
		reallocate(buf, limit, maxLimit, false);
	}

	@Override
	public void freeMemory() {
		bufFactory.freeMemory();
	}

	protected abstract PooledByteBuf allocate(ByteBufNew byteBufNew,int limit, int start, int end, int size);

	@Override
	protected void doStart() throws Exception {
		
		lock		= new ReentrantLock();

		bufFactory = createBufFactory();

		int capacity = this.capacity;

		initializeMemory(capacity * unitMemorySize);

		ByteBufUnit[] bufs = new ByteBufUnit[capacity];

		for (int i = 0; i < capacity; i++) {
			ByteBufUnit buf = new ByteBufUnit();
			buf.index = i;
			bufs[i] = buf;
		}

		this.units = bufs;
	}

	private ByteBufFactory createBufFactory() {
		if (isDirect) {
			return new DirectByteBufFactory();
		}
		return new HeapByteBufFactory();
	}

	@Override
	protected void doStop() throws Exception {
		this.freeMemory();
	}

	@Override
	public int getCapacity() {
		return capacity;
	}

	@Override
	public int getUnitMemorySize() {
		return unitMemorySize;
	}

	protected void initializeMemory(int capacity) {
		bufFactory.initializeMemory(capacity);
	}

	@Override
	public void release(ByteBuf buf) {

		ReentrantLock lock = this.lock;

		lock.lock();

		try {

			doRelease(units[((PooledByteBuf) buf).getBeginUnit()]);

		} finally {
			lock.unlock();
		}
	}

	protected abstract void doRelease(ByteBufUnit beginUnit);

	@Override
	public String toString() {

		busyUnit.clear();

		ByteBufUnit[] memoryUnits = this.units;

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
