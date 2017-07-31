package com.generallycloud.baseio.concurrent;

import com.generallycloud.baseio.component.Linkable;

public class McLinkedQueue<T extends Linkable<T>> extends ScLinkedQueue<T> {
	
	public McLinkedQueue() {
		this(new ReentrantLockImpl());
	}

	public McLinkedQueue(Lock lock) {
		this.lock = lock;
	}

	@Override
	public T poll() {
		if (size.get() == 0) {
			return null;
		}
		Lock lock = this.lock;
		lock.lock();
		try {
			if (size.get() == 0) {
				return null;
			}
			return get();
		} finally {
			lock.unlock();
		}
	}

}
