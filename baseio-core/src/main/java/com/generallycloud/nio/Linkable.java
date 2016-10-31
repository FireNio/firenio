package com.generallycloud.nio;

public interface Linkable<T> {

	public abstract Linkable<T> getNext();
	
	public abstract void setNext(Linkable<T> next);
	
	public abstract T getValue();
	
}
