package com.generallycloud.baseio.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ScBlockingQueue<E> {

	private final E[]		items;

	private int			takeIndex;

	private int			putIndex;
	
	private final	int		notFullCount;

	private AtomicInteger	count	= new AtomicInteger();

	private ReentrantLock	lock		= new ReentrantLock();

	private final Condition	notEmpty;

	private final Condition	notFull;

	private static void checkNotNull(Object v) {
		if (v == null)
			throw new NullPointerException();
	}

	private void enqueue(E x) {
		final Object[] items = this.items;
		items[putIndex] = x;
		if (++putIndex == items.length)
			putIndex = 0;
		count.incrementAndGet();
		notEmpty.signal();
	}

	private E dequeue() {
		E[] items = this.items;
		E x = items[takeIndex];
		items[takeIndex] = null;
		if (++takeIndex == items.length)
			takeIndex = 0;
		int c = count.decrementAndGet();
		if (c == notFullCount) {
			ReentrantLock lock = this.lock;
			lock.lock();
			try{
				notFull.signal();
			}finally{
				lock.unlock();
			}
		}
		return x;
	}

	@SuppressWarnings("unchecked")
	public ScBlockingQueue(int capacity) {
		if (capacity <= 0)
			throw new IllegalArgumentException();
		items = (E[]) new Object[capacity];
		notEmpty = lock.newCondition();
		notFull = lock.newCondition();
		notFullCount = capacity - 1;
	}

	public boolean offer(E e) {
		checkNotNull(e);
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			if (count.get() == items.length)
				return false;
			else {
				enqueue(e);
				return true;
			}
		} finally {
			lock.unlock();
		}
	}

	public void put(E e) throws InterruptedException {
		checkNotNull(e);
		final ReentrantLock lock = this.lock;
		lock.lockInterruptibly();
		try {
			while (count.get() == items.length)
				notFull.await();
			enqueue(e);
		} finally {
			lock.unlock();
		}
	}

	public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {

		checkNotNull(e);
		long nanos = unit.toNanos(timeout);
		final ReentrantLock lock = this.lock;
		lock.lockInterruptibly();
		try {
			while (count.get() == items.length) {
				if (nanos <= 0)
					return false;
				nanos = notFull.awaitNanos(nanos);
			}
			enqueue(e);
			return true;
		} finally {
			lock.unlock();
		}
	}

	public E poll() {
		if (count.get() == 0) {
			return null;
		}
		return dequeue();
	}

	public E take() throws InterruptedException {
		if (count.get() == 0) {
			final ReentrantLock lock = this.lock;
			lock.lockInterruptibly();
			try {
				while (count.get() == 0)
					notEmpty.await();
				return dequeue();
			} finally {
				lock.unlock();
			}
		}
		return dequeue();
	}

	public E poll(long timeout, TimeUnit unit) throws InterruptedException {
		if (count.get() == 0) {
			long nanos = unit.toNanos(timeout);
			final ReentrantLock lock = this.lock;
			lock.lockInterruptibly();
			try {
				while (count.get() == 0) {
					if (nanos <= 0)
						return null;
					nanos = notEmpty.awaitNanos(nanos);
				}
				return dequeue();
			} finally {
				lock.unlock();
			}
		}
		return dequeue();
	}

	public int size() {
		return count.get();
	}

	public int remainingCapacity() {
		return items.length - count.get();
	}

}
