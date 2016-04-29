package com.gifisan.nio.concurrent;

public interface LinkedList<T> {

	public abstract boolean offer(T object);
	
	public abstract void forceOffer(T object);

	public abstract T poll();

	public abstract T poll(long timeout);

	public abstract int size();

}