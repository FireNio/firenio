package com.generallycloud.baseio.concurrent;

import java.util.concurrent.atomic.AtomicInteger;

import com.generallycloud.baseio.component.Linkable;

public class ScLinkedQueue<T extends Linkable<T>> implements LinkedQueue<T> {

	protected Lock		lock;
	protected AtomicInteger	size = new AtomicInteger();		
	protected Linkable<T>	head	= null;	// volatile ?
	protected Linkable<T>	tail	= null;	// volatile ?
	
	public ScLinkedQueue() {
		this(new ReentrantLockImpl());
	}

	public ScLinkedQueue(Lock lock) {
		this.lock = lock;
	}

	@Override
	public void offer(Linkable<T> object) {
		Lock lock = this.lock;
		lock.lock();
		try {
			if (size.get() == 0) {
				head = tail = object;
			} else {
				tail.setNext(object);
				tail = object;
			}
			size.getAndIncrement();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public T poll() {
		int size = size();
		if (size == 0) {
			return null;
		} else if (size == 1) {
			Lock lock = this.lock;
			lock.lock();
			try {
				return get();
			} finally {
				lock.unlock();
			}
		}
		Linkable<T> t = head;
		head = t.getNext();
		this.size.getAndDecrement();
		return t.getValue();
	}

	protected T get() {
		Linkable<T> t = head;
		Linkable<T> next = t.getNext();
		if (next == null) {
			head = tail = null;
			size.set(0);
		} else {
			head = next;
			size.getAndDecrement();
		}
		return t.getValue();

	}

	@Override
	public int size() {
		return size.get();
	}

}
