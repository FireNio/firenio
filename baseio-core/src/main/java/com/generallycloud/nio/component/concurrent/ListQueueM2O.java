package com.generallycloud.nio.component.concurrent;

public class ListQueueM2O<T> extends AbstractListQueue<T> implements ListQueue<T> {

	private FixedAtomicInteger	_end;

	protected ListQueueM2O(int capability) {
		super(capability);
		_end = new FixedAtomicInteger(capability - 1);
	}

	protected ListQueueM2O() {
		super();
		_end = new FixedAtomicInteger(_capability - 1);
	}

	public final int getAndIncrementEnd() {
		return _end.getAndIncrement();
	}

}
