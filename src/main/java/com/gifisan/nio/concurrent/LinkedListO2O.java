package com.gifisan.nio.concurrent;

public class LinkedListO2O<T> extends AbstractLinkedList<T> implements LinkedList<T>{

	private int			_end			= -1;
	private int			_start		= 0;

	public LinkedListO2O(int _capability) {
		super(_capability);
	}
	
	public LinkedListO2O(){
		super();
	}
	
	public int incrementAndGet_end() {
		if (_end == _capability) {
			_end = 0;
		}
		return ++_end;
	}

	public final int getAndIncrement_start() {
		if (_start == _capability) {
			_start = 0;
		}
		return _start++;
	}
}
