/*
 * Copyright 2015 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
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
	public ByteBuf allocate(int limit, int maxLimit) {
		if (limit > maxLimit) {
			throw new BufferException("limit:"+limit+",maxLimit:"+maxLimit);
		}
		return allocate(limit);
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
	public ByteBuf reallocate(ByteBuf buf, int limit, boolean copyOld) {
		
		if (copyOld) {
			
			if (limit > buf.capacity()) {
				
				PooledByteBuf newBuf = allocate(bufFactory,limit);
				
				newBuf.read(buf.position(0));
				
				ReleaseUtil.release(buf);
				
				return buf.newByteBuf(this).produce(newBuf);
			}
			
			int oldLimit = buf.limit();
			
			return buf.limit(limit).skipBytes(oldLimit);
		}
		
		if (limit > buf.capacity()) {
			
			ReleaseUtil.release(buf);
			
			return allocate(buf, limit);
		}
		
		return buf.limit(limit);
	}

	@Override
	public ByteBuf reallocate(ByteBuf buf, int limit, int maxLimit, boolean copyOld) {
		if (limit > maxLimit) {
			throw new BufferException("limit:" + limit +",maxLimit:"+maxLimit);
		}
		return reallocate(buf,limit,copyOld);
	}

	@Override
	public ByteBuf reallocate(ByteBuf buf, int limit) {
		return reallocate(buf, limit, false);
	}

	@Override
	public ByteBuf reallocate(ByteBuf buf, int limit, int maxLimit) {
		return reallocate(buf, limit, maxLimit, false);
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
