package com.gifisan.nio.component.concurrent;

public class LinkedListO2O<T> extends AbstractLinkedList<T> implements LinkedList<T>{

	private int			_end			;

	public LinkedListO2O(int _capability) {
		super(_capability);
	}
	
	public LinkedListO2O(){
		super();
	}
	
	protected int getAndIncrementEnd() {
		if (_end == _capability) {
			_end = 0;
		}
		return _end++;
	}
	
	
}
