package com.generallycloud.nio;

public interface Linkable<T> {

	public abstract T getNext();
	
	public abstract void setNext(T next);
	
}
