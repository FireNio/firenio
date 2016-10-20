package com.generallycloud.nio.buffer;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

@Deprecated
public abstract class MemoryPoolV1 extends AbstractMemoryPool {

	private PooledByteBuf	memoryBlock;

	private MemoryUnitV1		memoryUnitStart;

	private MemoryUnitV1		memoryUnitEnd;

	private MemoryUnitV1[]	memoryUnits;

	protected ByteBuffer	memory;

	private int			capacity;

	protected void doStart() throws Exception {

		int capacity = this.capacity;

		this.memory = allocateMemory(capacity * unitMemorySize);

		memoryUnits = new MemoryUnitV1[capacity];

		MemoryUnitV1 next = memoryUnitStart;

		for (int i = 0; i < capacity; i++) {

			MemoryUnitV1 temp = new MemoryUnitV1(i);

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

		memoryBlock = new MemoryBlockV1(this, memory);

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

			PooledByteBuf next = memoryBlock;

			for (;;) {

				if (next == null) {
					return null;
				}

				if (next.getSize() < size) {

					next = next.getNext();

					continue;
				}

				PooledByteBuf r = new MemoryBlockV1(this, memory);

				MemoryUnitV1 start = next.getStart();

				MemoryUnitV1 end = memoryUnits[start.getIndex() + size - 1];

				r.setMemory(start, end);

				r.use();

				r.limit(capacity);

				next.setMemory(end.getNext(), next.getEnd());

				PooledByteBuf left = next.getPrevious();

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

	public MemoryPoolV1(int capacity) {
		this(capacity, 1024);
	}

	public MemoryPoolV1(int capacity, int unitMemorySize) {
		super(capacity, unitMemorySize);
	}

	public void release(ByteBuf memoryBlock) {

		PooledByteBuf _memoryBlock = (PooledByteBuf) memoryBlock;

		ReentrantLock lock = this.lock;

		lock.lock();

		try {

			_memoryBlock.free();

			MemoryUnitV1 start = _memoryBlock.getStart();
			MemoryUnitV1 end = _memoryBlock.getEnd();

			MemoryUnitV1 left = start.getPrevious();
			MemoryUnitV1 right = end.getNext();

			if (left != null && !left.isUsing()) {
				if (right != null && !right.isUsing()) {

					PooledByteBuf bLeft = _memoryBlock.getPrevious();
					PooledByteBuf bRight = _memoryBlock.getNext();

					MemoryUnitV1 newStart = bLeft.getStart();
					MemoryUnitV1 newEnd = bRight.getEnd();

					bLeft.setMemory(newStart, newEnd);
					bLeft.setNext(bRight);

					bRight.setPrevious(bLeft);

				} else {

					PooledByteBuf bLeft = _memoryBlock.getPrevious();

					MemoryUnitV1 newStart = bLeft.getStart();
					MemoryUnitV1 newEnd = _memoryBlock.getEnd();

					bLeft.setMemory(newStart, newEnd);
				}
			} else {

				if (right != null && !right.isUsing()) {

					PooledByteBuf bRight = _memoryBlock.getNext();

					MemoryUnitV1 newStart = _memoryBlock.getStart();
					MemoryUnitV1 newEnd = bRight.getEnd();

					bRight.setMemory(newStart, newEnd);
					bRight.setPrevious(_memoryBlock.getPrevious());

				} else {

					PooledByteBuf s = this.memoryBlock;

					int index = _memoryBlock.getEnd().getIndex();

					for (;;) {

						if (s.getEnd().getIndex() < index) {

							PooledByteBuf next = s.getNext();

							if (next == null) {

								s.setNext(_memoryBlock);

								return;
							}

							s = next;

							continue;
						}

						PooledByteBuf bLeft = s.getPrevious();
						PooledByteBuf bRight = s;

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
