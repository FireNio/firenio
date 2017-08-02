package com.generallycloud.baseio.concurrent;

import java.util.concurrent.atomic.AtomicInteger;

import com.generallycloud.baseio.common.UnsafeUtil;

public class ScmpLinkedQueueUnsafe<T extends Linkable<T>> implements LinkedQueue<T> {

	protected Linkable<T>	head	= null;				// volatile ?
	protected AtomicInteger	size	= new AtomicInteger();
	protected Linkable<T>	tail	= null;				// volatile ?
	protected final long	nextOffset;

	public ScmpLinkedQueueUnsafe(Linkable<T> linkable,long nextOffset) {
		linkable.setValidate(false);
		this.head = linkable;
		this.tail = linkable;
		this.nextOffset = nextOffset;
	}

	private T get(Linkable<T> h) {
		if (h.isValidate()) {
			Linkable<T> next = h.getNext();
			if (next == null) {
				h.setValidate(false);
			} else {
				head = next;
			}
			this.size.decrementAndGet();
			return h.getValue();
		} else {
			Linkable<T> next = h.getNext();
			head = next;
			return get(next);
		}
	}

	@Override
	public void offer(Linkable<T> linkable) {
		for (;;) {
			//FIXME 设置next后，设置tail
			if (UnsafeUtil.compareAndSwapObject(tail, nextOffset, null, linkable)) {
				tail = linkable;
				size.incrementAndGet();
				return;
			}
		}
	}
	
	@Override
	public T poll() {
		int size = size();
		if (size == 0) {
			return null;
		}
		return get(head);
	}

	@Override
	public int size() {
		return size.get();
	}

}
