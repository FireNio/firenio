package com.generallycloud.baseio.concurrent;

import com.generallycloud.baseio.component.Linkable;

public class McmpLinkedQueue<T extends Linkable<T>> extends ScmpLinkedQueue<T> {
	
	public McmpLinkedQueue() {
		this(new ReentrantLockImpl());
	}

	public McmpLinkedQueue(Lock lock) {
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
