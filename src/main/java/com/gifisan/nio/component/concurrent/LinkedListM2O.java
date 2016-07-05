package com.gifisan.nio.component.concurrent;

public class LinkedListM2O<T> extends AbstractLinkedList<T> implements LinkedList<T> {

	private FixedAtomicInteger	_end;

	public LinkedListM2O(int capability) {
		super(capability);
		_end = new FixedAtomicInteger(capability - 1);
	}

	public LinkedListM2O() {
		super();
		_end = new FixedAtomicInteger(_capability - 1);
	}

	public final int getAndIncrementEnd() {
		return _end.getAndIncrement();
	}

}
