package com.gifisan.nio.concurrent;

import java.util.concurrent.atomic.AtomicInteger;

public class LinkedListM2O<T> extends AbstractLinkedList<T> implements LinkedList<T>{

	private AtomicInteger	_end			= new AtomicInteger(-1);
	private int			_start		= 0;

	public LinkedListM2O(int _capability) {
		super(_capability);
	}
	
	public LinkedListM2O() {
		super();
	}

	public final int getAndIncrement_start() {
		if (_start == _capability) {
			_start = 0;
		}
		return _start++;
	}

	public final int incrementAndGet_end() {
		for (;;) {
			int current = _end.get();

			int next = current + 1;

			if (next == _capability) {
				next = 0;
			}

			if (_end.compareAndSet(current, next))
				return next;
		}
	}
}
