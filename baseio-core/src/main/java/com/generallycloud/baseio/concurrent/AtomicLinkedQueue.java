package com.generallycloud.baseio.concurrent;

import com.generallycloud.baseio.component.Linkable;

public class AtomicLinkedQueue<T extends Linkable<T>> {

	private AtomicLock	lock	= new AtomicLock();
	private int		size;
	private Linkable<T>	head	= null;
	private Linkable<T>	tail	= null;

	public boolean offer(Linkable<T> object) {
		AtomicLock lock = this.lock;
		lock.tryLock();
		try {
			if (size == 0) {
				head = tail = object;
			} else {
				tail.setNext(object);
				tail = object;
			}
			size++;
			return true;
		} finally {
			lock.unlock();
		}
	}

	public T poll() {
		AtomicLock lock = this.lock;
		lock.tryLock();
		try {
			if (size == 0) {
				return null;
			}
			return get();
		} finally {
			lock.unlock();
		}
	}

	private T get() {
		Linkable<T> t = head;
		Linkable<T> next = t.getNext();
		if (next == null) {
			head = tail = null;
		} else {
			head = next;
		}
		size--;
		return t.getValue();

	}

	public int size() {
		return size;
	}

}
