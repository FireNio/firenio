package com.gifisan.nio.concurrent;

import java.util.concurrent.atomic.AtomicInteger;

public class LinkedListO2M<T> extends AbstractLinkedList<T> implements LinkedList<T> {

	private int			_end			= -1;
	private AtomicInteger	_start		= new AtomicInteger(0);

	public LinkedListO2M(int _capability) {
		super(_capability);
	}
	
	public LinkedListO2M() {
		super();
	}

	public final int incrementAndGet_end() {
		if (_end == _capability) {
			_end = 0;
		}
		return ++_end;
	}

	public final int getAndIncrement_start() {
		for (;;) {
			int current = _start.get();

			int next = current + 1;

			if (next == _capability) {
				next = 0;
			}

			if (_start.compareAndSet(current, next))
				return current;
		}
	}
}
