package com.generallycloud.nio.component.concurrent;

import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.Linkable;

public class ListQueueLink<T extends Linkable<T>> implements ListQueue<T> {

	private ReentrantLock	lock	= new ReentrantLock();

	private int			size;

	private Linkable<T>			head	= null;

	private Linkable<T>			tail	= null;

	public boolean offer(T object) {

		ReentrantLock lock = this.lock;

		lock.lock();

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

		ReentrantLock lock = this.lock;

		lock.lock();

		try {

			if (size == 0) {
				return null;
			}

			Linkable<T> t = head;
			Linkable<T> next = t.getNext();

			if (next == null) {
				head = tail = null;
			} else {
				head = next;
			}

			size--;
			return t.getValue();

		} finally {
			lock.unlock();
		}
	}

	public T poll(long timeout) {
		throw new UnsupportedOperationException();
	}

	public int size() {
		return size;
	}

}
