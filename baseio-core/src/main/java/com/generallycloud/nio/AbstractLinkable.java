package com.generallycloud.nio;

public abstract class AbstractLinkable<T> implements Linkable<T>{

	private Linkable<T> next;
	
	private T value;

	public AbstractLinkable(T value) {
		this.value = value;
	}

	@Override
	public Linkable<T> getNext() {
		return next;
	}

	@Override
	public void setNext(Linkable<T> next) {
		this.next = next;
	}

	@Override
	public T getValue() {
		return value;
	}

	
}
