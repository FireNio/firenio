package com.generallycloud.nio.component.protocol;


public interface Future {

	public abstract void attach(Object attachment);

	public abstract Object attachment();
}
