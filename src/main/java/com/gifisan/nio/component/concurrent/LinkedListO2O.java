package com.gifisan.nio.component.concurrent;

public class LinkedListO2O<T> extends AbstractLinkedList<T> implements LinkedList<T>{

	private int			_end			;
	private int			_start		;

	public LinkedListO2O(int _capability) {
		super(_capability);
	}
	
	public LinkedListO2O(){
		super();
	}
	
	protected int getAndincrementStart() {
		if (_start == _capability) {
			_start = 0;
		}
		return _start++;
	}

	protected int getAndincrementEnd() {
		if (_end == _capability) {
			_end = 0;
		}
		return _end++;
	}
	
	
}
