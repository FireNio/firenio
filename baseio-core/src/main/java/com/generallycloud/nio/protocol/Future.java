package com.generallycloud.nio.protocol;

import com.generallycloud.nio.Releasable;

public interface Future extends Releasable{

	public abstract void attach(Object attachment);

	public abstract Object attachment();
}
