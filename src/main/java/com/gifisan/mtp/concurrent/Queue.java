package com.gifisan.mtp.concurrent;

public interface Queue<T> {

	public abstract T poll();
	
	public abstract void offer(T t);
	
	public abstract boolean empty();
	
}
