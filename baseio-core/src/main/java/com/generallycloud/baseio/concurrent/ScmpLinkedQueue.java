package com.generallycloud.baseio.concurrent;

import java.util.concurrent.atomic.AtomicInteger;

import com.generallycloud.baseio.log.DebugUtil;

public class ScmpLinkedQueue<T> implements LinkedQueue<T> {

	protected Linkable		head	= null;				// volatile ?
	protected Lock		lock	= null;
	protected AtomicInteger	size	= new AtomicInteger();
	protected Linkable		tail	= null;				// volatile ?

	public ScmpLinkedQueue(Linkable linkable) {
		this(linkable, new ReentrantLockImpl());
	}

	public ScmpLinkedQueue(Linkable linkable, Lock lock) {
		linkable.setValidate(false);
		this.head = linkable;
		this.tail = linkable;
		this.lock = lock;
	}

	@SuppressWarnings("unchecked")
	private T get(Linkable h) {
		if (h.isValidate()) {
			Linkable next = h.getNext();
			if (next == null) {
				h.setValidate(false);
			} else {
				head = next;
			}
			this.size.decrementAndGet();
			return (T) h;
		} else {
			Linkable next = h.getNext();
			if (next == null) {
				DebugUtil.info("------------------");
			}
			head = next;
			return get(next);
		}
	}

	public void offer(Linkable object) {
		Lock lock = this.lock;
		lock.lock();
		try {
			tail.setNext(object);
			tail = object;
		} finally {
			lock.unlock();
		}
		size.incrementAndGet();
	}

	public T poll() {
		int size = size();
		if (size == 0) {
			return null;
		}
		return get(head);
	}

	public int size() {
		return size.get();
	}

}
