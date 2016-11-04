package com.generallycloud.nio.buffer.v2;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.buffer.AbstractMemoryPool;
import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.buffer.v1.PooledByteBufV1;

@Deprecated
public abstract class MemoryPoolV2 extends AbstractMemoryPool {

	private PooledByteBufV1	memoryBlock;

	private MemoryUnitV2		memoryUnitStart;

	private MemoryUnitV2		memoryUnitEnd;

	private MemoryUnitV2[]	memoryUnits;

	protected ByteBuffer	memory;

	private int			capacity;

	protected void doStart() throws Exception {

		int capacity = this.capacity;

		this.memory = allocateMemory(capacity * unitMemorySize);

		memoryUnits = new MemoryUnitV2[capacity];

		MemoryUnitV2 next = memoryUnitStart;

		for (int i = 0; i < capacity; i++) {

			MemoryUnitV2 temp = new MemoryUnitV2(i);

			memoryUnits[i] = temp;

			if (next == null) {

				next = temp;

				memoryUnitStart = temp;

				continue;
			}

			next.setNext(temp);

			temp.setPrevious(next);

			next = temp;
		}

		memoryUnitEnd = next;

		memoryBlock = new MemoryBlockV2(this, memory);

		memoryBlock.setMemory(memoryUnitStart, memoryUnitEnd);

	}

	protected void doStop() throws Exception {
		this.freeMemory();
	}

	public ByteBuf allocate(int capacity) {

		int size = (capacity + unitMemorySize - 1) / unitMemorySize;

		ReentrantLock lock = this.lock;

		lock.lock();

		try {

			PooledByteBufV1 next = memoryBlock;

			for (;;) {

				if (next == null) {
					return null;
				}

				if (next.getSize() < size) {

					next = next.getNext();

					continue;
				}

				PooledByteBufV1 r = new MemoryBlockV2(this, memory);

				MemoryUnitV2 start = next.getStart();

				MemoryUnitV2 end = memoryUnits[start.getIndex() + size - 1];

				r.setMemory(start, end);

				r.use();

				r.limit(capacity);

				next.setMemory(end.getNext(), next.getEnd());

				PooledByteBufV1 left = next.getPrevious();

				if (left != null) {
					r.setPrevious(left);
					left.setNext(r);
				}

				r.setNext(next);
				next.setPrevious(r);

				return r;
			}

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

		PooledByteBufV1 _memoryBlock = (PooledByteBufV1) memoryBlock;

		ReentrantLock lock = this.lock;

		lock.lock();

		try {

			_memoryBlock.free();

			MemoryUnitV2 start = _memoryBlock.getStart();
			MemoryUnitV2 end = _memoryBlock.getEnd();

			MemoryUnitV2 left = start.getPrevious();
			MemoryUnitV2 right = end.getNext();

			if (left != null && !left.isUsing()) {
				if (right != null && !right.isUsing()) {

					PooledByteBufV1 bLeft = _memoryBlock.getPrevious();
					PooledByteBufV1 bRight = _memoryBlock.getNext();

					MemoryUnitV2 newStart = bLeft.getStart();
					MemoryUnitV2 newEnd = bRight.getEnd();

					bLeft.setMemory(newStart, newEnd);
					bLeft.setNext(bRight);

					bRight.setPrevious(bLeft);

				} else {

					PooledByteBufV1 bLeft = _memoryBlock.getPrevious();

					MemoryUnitV2 newStart = bLeft.getStart();
					MemoryUnitV2 newEnd = _memoryBlock.getEnd();

					bLeft.setMemory(newStart, newEnd);
				}
			} else {

				if (right != null && !right.isUsing()) {

					PooledByteBufV1 bRight = _memoryBlock.getNext();

					MemoryUnitV2 newStart = _memoryBlock.getStart();
					MemoryUnitV2 newEnd = bRight.getEnd();

					bRight.setMemory(newStart, newEnd);
					bRight.setPrevious(_memoryBlock.getPrevious());

				} else {

					PooledByteBufV1 s = this.memoryBlock;

					int index = _memoryBlock.getEnd().getIndex();

					for (;;) {

						if (s.getEnd().getIndex() < index) {

							PooledByteBufV1 next = s.getNext();

							if (next == null) {

								s.setNext(_memoryBlock);

								return;
							}

							s = next;

							continue;
						}

						PooledByteBufV1 bLeft = s.getPrevious();
						PooledByteBufV1 bRight = s;

						_memoryBlock.setPrevious(bLeft);
						bLeft.setNext(_memoryBlock);

						_memoryBlock.setNext(bRight);
						bRight.setPrevious(_memoryBlock);

						return;
					}
				}
			}

		} finally {
			lock.unlock();
		}
	}
}
