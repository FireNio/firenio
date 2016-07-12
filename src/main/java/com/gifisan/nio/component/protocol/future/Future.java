package com.gifisan.nio.component.protocol.future;


public interface Future {

	public abstract void attach(Object attachment);

	public abstract Object attachment();
}
