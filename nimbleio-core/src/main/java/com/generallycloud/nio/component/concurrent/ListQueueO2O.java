package com.generallycloud.nio.component.concurrent;

public class ListQueueO2O<T> extends AbstractListQueue<T> implements ListQueue<T>{

	private int			_end			;

	protected ListQueueO2O(int _capability) {
		super(_capability);
	}
	
	protected ListQueueO2O(){
		super();
	}
	
	protected int getAndIncrementEnd() {
		if (_end == _capability) {
			_end = 0;
		}
		return _end++;
	}
	
	
}
