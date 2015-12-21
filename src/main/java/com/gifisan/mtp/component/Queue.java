package com.gifisan.mtp.component;

public interface Queue<T> {

	public abstract T poll();
	
	public abstract void offer(T t);
	
	public abstract boolean empty();
	
}
