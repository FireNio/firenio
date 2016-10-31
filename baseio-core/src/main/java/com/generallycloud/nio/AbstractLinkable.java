package com.generallycloud.nio;

public abstract class AbstractLinkable<T> implements Linkable<T>{

	private Linkable<T> next;
	
	private T value;

	public AbstractLinkable(T value) {
		this.value = value;
	}

	public Linkable<T> getNext() {
		return next;
	}

	public void setNext(Linkable<T> next) {
		this.next = next;
	}

	public T getValue() {
		return value;
	}

	
}
